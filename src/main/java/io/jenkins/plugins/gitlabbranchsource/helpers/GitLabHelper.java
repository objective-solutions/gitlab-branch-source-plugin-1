package io.jenkins.plugins.gitlabbranchsource.helpers;

import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.UriTemplateBuilder;
import com.damnhandy.uri.template.impl.Operator;
import hudson.ProxyConfiguration;
import io.jenkins.plugins.gitlabserverconfig.credentials.PersonalAccessToken;
import io.jenkins.plugins.gitlabserverconfig.servers.GitLabServer;
import io.jenkins.plugins.gitlabserverconfig.servers.GitLabServers;
import java.util.Map;
import jenkins.model.Jenkins;
import org.eclipse.jgit.annotations.NonNull;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.ProxyClientConfig;

public class GitLabHelper {

    public static GitLabApi apiBuilder(String serverName) {
        GitLabServer server = GitLabServers.get().findServer(serverName);
        if (server != null) {
            PersonalAccessToken credentials = server.getCredentials();
            if (credentials != null) {
                return new GitLabApi(server.getServerUrl(), credentials.getToken().getPlainText(), null, getProxyConfig());
            }
            return new GitLabApi(server.getServerUrl(), GitLabServer.EMPTY_TOKEN, null, getProxyConfig());
        }
        throw new IllegalStateException(
            String.format("No server found with the name: %s", serverName));
    }

    public static Map<String, Object> getProxyConfig () {
        ProxyConfiguration proxyConfiguration = Jenkins.get().getProxy();
        if (proxyConfiguration != null) {
            if (proxyConfiguration.getUserName() != null && proxyConfiguration.getSecretPassword() != null) {
                return ProxyClientConfig.createProxyClientConfig(
                    "http://" + proxyConfiguration.getName() + ":" + proxyConfiguration.getPort(),
                    proxyConfiguration.getUserName(),
                    proxyConfiguration.getSecretPassword().getPlainText());
            }
            return ProxyClientConfig.createProxyClientConfig(
                "http://" + proxyConfiguration.getName() + ":" + proxyConfiguration.getPort());
        }
        return null;
    }

    @NonNull
    public static String getServerUrlFromName(String serverName) {
        GitLabServer server = GitLabServers.get().findServer(serverName);
        return getServerUrl(server);
    }

    @NonNull
    public static String getServerUrl(GitLabServer server) {
        return server != null ? server.getServerUrl() : GitLabServer.GITLAB_SERVER_URL;
    }

    @NonNull
    private static String getServerUrl(String server) {
        if (server.startsWith("http://") || server.startsWith("https://")) {
            return server;
        } else {
            return getServerUrlFromName(server);
        }
    }

    public static UriTemplateBuilder getUriTemplateFromServer(String server) {
        return UriTemplate.buildFromTemplate(getServerUrl(server));
    }

    public static UriTemplate projectUriTemplate(String serverNameOrUrl) {
        return getUriTemplateFromServer(serverNameOrUrl)
            .template("{/project*}").build();
    }

    public static UriTemplate branchUriTemplate(String serverNameOrUrl) {
        return getUriTemplateFromServer(serverNameOrUrl)
            .template("{/project*}/tree/{branch*}").build();
    }

    public static UriTemplate mergeRequestUriTemplate(String serverNameOrUrl) {
        return getUriTemplateFromServer(serverNameOrUrl)
            .template("{/project*}/merge_requests/{iid}").build();
    }

    public static UriTemplate tagUriTemplate(String serverNameOrUrl) {
        return getUriTemplateFromServer(serverNameOrUrl)
            .template("{/project*}/tree/{tag*}").build();
    }

    public static UriTemplate commitUriTemplate(String serverNameOrUrl) {
        return getUriTemplateFromServer(serverNameOrUrl)
            .template("{/project*}/commit/{hash}")
            .build();
    }

    public static String[] splitPath(String path) {
        return path.split(Operator.PATH.getSeparator());
    }

}
