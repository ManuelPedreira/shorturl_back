package com.manuelpedreira.shorturl.services.helpers;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SafeUrlValidator {

    private final String publicHost;

    public SafeUrlValidator(@Value("${server.public.host}") String publicHost) {
        this.publicHost = publicHost;
    }

    public boolean isSafeUrl(String url) {
        try {
            return isSafeUrl(new URI(url));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }

    public boolean isSafeUrl(URI uri) {
        if (uri == null)
            return false;

        int port = uri.getPort();
        if (port != -1 && port != 80 && port != 443)
            return false;

        String scheme = uri.getScheme();
        if (scheme == null || !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
            return false;

        String host = uri.getHost();
        if (host == null)
            return false;

        if (publicHost != null && publicHost.contains(host)) // same host name
            return false;

        try {
            InetAddress[] addrs = InetAddress.getAllByName(host);
            for (InetAddress addr : addrs) {
                if (addr.isAnyLocalAddress() // 0.0.0.0
                        || addr.isLoopbackAddress() // 127.0.0.1 ::1
                        || addr.isLinkLocalAddress() // 169.254.x.x, fe80::
                        || addr.isSiteLocalAddress() // 10.x.x.x, 192.168.x.x, 172.16.x.x
                ) {
                    return false;
                }
            }
        } catch (UnknownHostException e) {
            return false;
        }
        return true;
    }
}