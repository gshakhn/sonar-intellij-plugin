package org.sonar.ide.intellij.utils;

import com.intellij.util.net.HttpConfigurable;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.connectors.HttpClient4Connector;

public class SonarUtils {
  public static String fixHostName(String hostName) {
    String host = hostName;
    if (host.indexOf("://") == -1)
      host = "http://" + host;
    if (host.charAt(host.length()-1) == '/')
      host = host.substring(0, host.length()-1);
    return host;
  }

    /**
     * Utility method to retrieve a new Sonar connection using specified credentials and eventually the global proxy.
     * @param host url of sonar host
     * @param user (optional) username to access sonar
     * @param password (optional) password to access sonar
     * @param useProxy flag to indicate whether IDEA's proxy settings shall be used to connect to Sonar
     * @return a connection to Sonar
     */
    public static Sonar getSonar(String host, String user, String password, boolean useProxy)
    {
        Host hostServer = new Host(fixHostName(host));
        // use credentials for Sonar in case they are specified
        if (user != null && password != null)
        {
            hostServer.setUsername(user);
            hostServer.setPassword(password);
        }
        final HttpClient4Connector connector = new HttpClient4Connector(hostServer);
        // check whether IDEA has a proxy set
        HttpConfigurable proxySettings = HttpConfigurable.getInstance();
        if (useProxy && proxySettings.USE_HTTP_PROXY)
        {
            DefaultHttpClient httpClient = connector.getHttpClient();
            // set proxy authentication if needed
            if (proxySettings.PROXY_AUTHENTICATION)
            {
                AuthScope authScope = new AuthScope(proxySettings.PROXY_HOST, proxySettings.PROXY_PORT);
                UsernamePasswordCredentials proxyCredentials = new UsernamePasswordCredentials(
                        proxySettings.PROXY_LOGIN, proxySettings.getPlainProxyPassword());

                httpClient.getCredentialsProvider().setCredentials(authScope, proxyCredentials);
            }
            HttpHost proxy = new HttpHost(proxySettings.PROXY_HOST, proxySettings.PROXY_PORT);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        return new Sonar(connector);
    }

}
