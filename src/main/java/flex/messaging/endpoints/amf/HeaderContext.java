package flex.messaging.endpoints.amf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.MessageHeader;

/**
 * amf header context
 * an threadLoacl variable
 */
public class HeaderContext extends AMFFilter {
    
    private static ThreadLocal<List<MessageHeader>> amfHeaders = new ThreadLocal<List<MessageHeader>>();
    
    /**
     * the value of a header can be any Object;
     */
    @SuppressWarnings("unchecked")
    public static <T> T getHeader(String key){
        List<MessageHeader> headers = amfHeaders.get();
        if(headers == null) return null;
        
        for (MessageHeader header : headers) {
            if(header.getName().equals(key)){
                return (T) header.getData();
            }
        }
        
        return null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void invoke(ActionContext context) throws IOException {
        addHeaders(context.getRequestMessage().getHeaders());
        
        next.invoke(context);
    }

    private void addHeaders(ArrayList<MessageHeader> headers) {
    	/*System.out.print("addHeaders...");
    	StringBuilder sb = new StringBuilder();
    	for(int i=0;i<headers.size();i++){
    		MessageHeader header = headers.get(i);
    		sb.append("【"+header.getName()+":"+header.getData()+"】");
    		if(i != headers.size()-1){
    			sb.append(",");
    		}
    	}
    	System.out.println(sb.toString());*/
        amfHeaders.set(headers);
    }

    public static void clearHeaders() {
    	//System.out.println("clearHeaders...\n");
        amfHeaders.remove();
    }

}
