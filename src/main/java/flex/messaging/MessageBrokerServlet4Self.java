package flex.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import flex.messaging.config.ChannelSettings;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.FlexConfigurationManager;
import flex.messaging.config.MessagingConfiguration;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.endpoints.amf.HeaderContext;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.log.Logger;
import flex.messaging.log.ServletLogTarget;
import flex.messaging.services.AuthenticationService;
import flex.messaging.util.ClassUtil;
import flex.messaging.util.ExceptionUtil;
import flex.messaging.util.Trace;

/**
 * Hacked an MessageBrokerServlet
 * Add a function for close the FlexSession
 */
public class MessageBrokerServlet4Self extends MessageBrokerServlet {

    private static final long serialVersionUID = -2509928967558653217L;

    private MessageBroker broker;

    private static String FLEXDIR = "/WEB-INF/flex/";

    private boolean enableFlexSession = false;

    private transient ServletConfig config;

    public ServletConfig getConfig() {
        return config;
    }

    /**
     * Initializes the servlet in its web container, then creates the
     * MessageBroker and adds Endpoints and Services to that broker. This
     * servlet may keep a reference to an endpoint if it needs to delegate to it
     * in the <code>service</code> method.
     */
    public void init(ServletConfig servletConfig) throws ServletException, UnavailableException {
        config = servletConfig;

        // allocate thread local variables
        createThreadLocals();

        // Set the servlet config as thread local
        FlexContext.setThreadLocalObjects(null, null, null, null, null, servletConfig);

        ServletLogTarget.setServletContext(servletConfig.getServletContext());

        ClassLoader loader = getClassLoader();

        String useCCLoader;

        if ((useCCLoader = servletConfig.getInitParameter("useContextClassLoader")) != null
                && useCCLoader.equalsIgnoreCase("true"))
            loader = Thread.currentThread().getContextClassLoader();

        // Start the broker
        try {
            // Get the configuration manager
            ConfigurationManager configManager = loadMessagingConfiguration(servletConfig);

            // Load configuration
            MessagingConfiguration config = configManager.getMessagingConfiguration(servletConfig);

            // Set up logging system ahead of everything else.
            config.createLogAndTargets();

            // Create broker.
            broker = config.createBroker(servletConfig.getInitParameter("messageBrokerId"), loader);

            // Set the servlet config as thread local
            FlexContext.setThreadLocalObjects(null, null, broker, null, null, servletConfig);

            setupInternalPathResolver();

            // Set initial servlet context on broker
            broker.setInitServletContext(servletConfig.getServletContext());

            Logger logger = Log.getLogger(ConfigurationManager.LOG_CATEGORY);
            if (Log.isInfo()) {
                logger.info(VersionInfo.buildMessage());
            }

            // Create endpoints, services, security, and logger on the broker
            // based on configuration
            config.configureBroker(broker);

            long timeBeforeStartup = 0;
            if (Log.isDebug()) {
                timeBeforeStartup = System.currentTimeMillis();
                Log.getLogger(LOG_CATEGORY_STARTUP_BROKER).debug("MessageBroker with id '{0}' is starting.",
                        new Object[] { broker.getId() });
            }

            // initialize the httpSessionToFlexSessionMap
            synchronized (HttpFlexSession.mapLock) {
                if (servletConfig.getServletContext().getAttribute(HttpFlexSession.SESSION_MAP) == null)
                    servletConfig.getServletContext()
                            .setAttribute(HttpFlexSession.SESSION_MAP, new ConcurrentHashMap());
            }

            broker.start();

            if (Log.isDebug()) {
                long timeAfterStartup = System.currentTimeMillis();
                Long diffMillis = new Long(timeAfterStartup - timeBeforeStartup);
                Log.getLogger(LOG_CATEGORY_STARTUP_BROKER).debug(
                        "MessageBroker with id '{0}' is ready (startup time: '{1}' ms)",
                        new Object[] { broker.getId(), diffMillis });
            }

            //(1)读取到了该配置就可以在service方法中控制httpFlexSession是否关闭了
            ChannelSettings channelSettings = config.getChannelSettings("channel-amf");
            ConfigMap properties = channelSettings.getProperties();
            String str = (String) properties.get("flexSession-enabled");
            if (str != null) {
                enableFlexSession = Boolean.valueOf(str);
            }

            // Report replaced tokens
            configManager.reportTokens();

            // Report any unused properties.
            config.reportUnusedProperties();

            // clear the broker and servlet config as this thread is done
            FlexContext.clearThreadLocalObjects();

        } catch (Throwable t) {
            // On any unhandled exception destroy the broker, log it and
            // rethrow.
            System.err.println("**** MessageBrokerServlet failed to initialize due to runtime exception: "
                    + ExceptionUtil.exceptionFollowedByRootCausesToString(t));
            destroy();
            throw new UnavailableException(t.getMessage());
        }
    }

    private void setupInternalPathResolver() {
        broker.setInternalPathResolver(new MessageBroker.InternalPathResolver() {
            public InputStream resolve(String filename) {
                InputStream is = getServletContext().getResourceAsStream(FLEXDIR + filename);
                return is;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ConfigurationManager loadMessagingConfiguration(ServletConfig servletConfig) {
        ConfigurationManager manager = null;
        Class managerClass = null;
        String className = null;

        // Check for Custom Configuration Manager Specification
        if (servletConfig != null) {
            String p = servletConfig.getInitParameter("services.configuration.manager");
            if (p != null) {
                className = p.trim();
                try {
                    managerClass = ClassUtil.createClass(className);
                    manager = (ConfigurationManager) managerClass.newInstance();
                } catch (Throwable t) {
                    if (Trace.config) // Log is not initialized yet.
                        Trace.trace("Could not load configuration manager as: " + className);
                }
            }
        }

        if (manager == null) {
            manager = (ConfigurationManager) new FlexConfigurationManager();
        }

        return manager;
    }

    /**
     * Handle an incoming request, and delegate to an endpoint based on content
     * type, if appropriate. The content type mappings for endpoints are not
     * externally configurable, and currently the AmfEndpoint is the only
     * delegate.
     */
    public void service(HttpServletRequest req, HttpServletResponse res) {
        try {
            // Update thread locals
            broker.initThreadLocals();
            // Set this first so it is in place for the session creation event.
            // The
            // current session is set by the FlexSession stuff right when it is
            // available.
            // The threadlocal FlexClient is set up during message
            // deserialization in the
            // MessageBrokerFilter.
            FlexContext.setThreadLocalObjects(null, null, broker, req, res, getServletConfig());
            Principal principal = null;
            if (FlexContext.isPerClientAuthentication()) {
                principal = FlexContext.getUserPrincipal();
            } else {
            	//(2)判断是否需要httpFlexSession如果不需要则不创建httpFlexSession
                if (enableFlexSession) {
                    HttpFlexSession fs = HttpFlexSession.getFlexSession(req);
                    principal = fs.getUserPrincipal();
                }
            }

            if (principal == null && req.getHeader("Authorization") != null) {
                String encoded = req.getHeader("Authorization");
                if (encoded.indexOf("Basic") > -1) {
                    encoded = encoded.substring(6); // Basic.length()+1
                    try {
                        AuthenticationService.decodeAndLogin(encoded, broker.getLoginManager());
                    } catch (Exception e) {
                        if (Log.isDebug())
                            Log.getLogger(LogCategories.SECURITY).info(
                                    "Authentication service could not decode and login: " + e.getMessage());
                    }
                }
            }

            String contextPath = req.getContextPath();
            String pathInfo = req.getPathInfo();
            String endpointPath = req.getServletPath();
            if (pathInfo != null)
                endpointPath = endpointPath + pathInfo;

            Endpoint endpoint = null;
            try {
                endpoint = broker.getEndpoint(endpointPath, contextPath);
            } catch (MessageException me) {
                if (Log.isInfo())
                    Log.getLogger(LogCategories.ENDPOINT_GENERAL).info(
                            "Received invalid request for endpoint path '{0}'.", new Object[] { endpointPath });

                if (!res.isCommitted()) {
                    try {
                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } catch (IOException ignore) {
                    }
                }
                return;
            }

            try {
                if (Log.isInfo()) {
                    Log.getLogger(LogCategories.ENDPOINT_GENERAL).info("Channel endpoint {0} received request.",
                            new Object[] { endpoint.getId() });
                }
                endpoint.service(req, res);
            } catch (UnsupportedOperationException ue) {
                if (Log.isInfo()) {
                    Log.getLogger(LogCategories.ENDPOINT_GENERAL).info(
                            "Channel endpoint {0} received request for an unsupported operation.",
                            new Object[] { endpoint.getId() }, ue);
                }

                if (!res.isCommitted()) {
                    try {
                        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    } catch (IOException ignore) {
                    }
                }
            }
        } finally {
            FlexContext.clearThreadLocalObjects();
            //(3)清除AMF协议头部信息，确保验证完令牌后从线程内存中清除，清除目的如下
            //1、由于tomcat线程重用导致的头部信息错乱；2、避免为每个线程存储那么多的头部信息
            HeaderContext.clearHeaders();
        }
    }

}
