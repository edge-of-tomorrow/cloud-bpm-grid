package org.kie.server.util;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class SimpleRESTClient {

    private static SimpleRESTClient instance;
    
    private String user;
    private String pwd;

    private SimpleRESTClient(String user, String pwd) {
        this.user = user;
        this.pwd = pwd;
    }

    public static SimpleRESTClient getInstance(String user, String pwd) {
        if (instance == null) {
            instance = new SimpleRESTClient(user, pwd);
        }
        return instance;
    }

    public String getContainerIdFromTaskId(long taskId) {
        String containerId = "default";
        // request in the docker container
        String requestUrl = "http://localhost:8181/kie-server/services/rest/server/queries/tasks/instances/" + taskId;
        
        HttpHost targetHost = new HttpHost("localhost", 8181, "http");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, 
          new UsernamePasswordCredentials(user, pwd));
         
        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());
         
        // Add AuthCache to the execution context
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(requestUrl);
            request.addHeader("Accept", "application/json");
            
            HttpResponse result = httpClient.execute(request, context);

            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            JsonParser parser = new JsonFactory().createJsonParser(json);
            while(!parser.isClosed()) {
                JsonToken token = parser.nextToken();
                if(JsonToken.FIELD_NAME.equals(token)){
                    String name = parser.getCurrentName();
                    if ("task-container-id".equals(name)) {
                        containerId = parser.nextTextValue();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return containerId;
    }
    
    /**public static void main(String[] args) {
        String containerId = SimpleRESTClient.getInstance("communicator", "communicator1234!").getContainerIdFromTaskId(77);
        System.out.println(containerId);
    }*/

}
