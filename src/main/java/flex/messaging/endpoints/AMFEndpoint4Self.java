package flex.messaging.endpoints;

import flex.messaging.endpoints.amf.AMFFilter;
import flex.messaging.endpoints.amf.BatchProcessFilter;
import flex.messaging.endpoints.amf.HeaderContext;
import flex.messaging.endpoints.amf.LegacyFilter;
import flex.messaging.endpoints.amf.MessageBrokerFilter;
import flex.messaging.endpoints.amf.SerializationFilter;
import flex.messaging.endpoints.amf.SessionFilter;

/**
 * Hacked an Endpoint extends from AMFEndpoint
 * Has all the AMFEndpoint functions
 */
public class AMFEndpoint4Self extends AMFEndpoint {
    
    @Override
    protected AMFFilter createFilterChain() {
    	//System.out.println("createFilterChain begin...");
        AMFFilter serializationFilter = new SerializationFilter(getLogCategory());
        AMFFilter headerFilter = new HeaderContext();
        AMFFilter batchFilter = new BatchProcessFilter();
        AMFFilter sessionFilter = new SessionFilter();
        AMFFilter envelopeFilter = new LegacyFilter(this);
        AMFFilter messageBrokerFilter = new MessageBrokerFilter(this);

        serializationFilter.setNext(headerFilter);
        headerFilter.setNext(batchFilter);
        batchFilter.setNext(sessionFilter);
        sessionFilter.setNext(envelopeFilter);
        envelopeFilter.setNext(messageBrokerFilter);

        //System.out.println("createFilterChain end...");
        return serializationFilter;
    }
    
}
