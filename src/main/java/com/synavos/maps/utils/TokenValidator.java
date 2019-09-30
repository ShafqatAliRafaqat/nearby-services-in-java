package com.synavos.maps.utils;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.synavos.maps.properties.GoogleMapProperties;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenValidator {

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
	TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

	SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
		.build();

	SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

	CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

	HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

	requestFactory.setHttpClient(httpClient);
	RestTemplate restTemplate = new RestTemplate(requestFactory);
	return restTemplate;
    }

    public boolean isValidToken(final String token) {
	boolean valid = true;

	if (GoogleMapProperties.VALIDATE_TOKEN) {
	    log.debug(log.isDebugEnabled() ? StringUtils.concatValues("Validating token [", token, "]") : null);

	    valid = false;

	    if (!StringUtils.isNullOrEmptyStr(token)) {
		try {
		    final RequestEntity<Void> request = RequestEntity.get(new URI(GoogleMapProperties.VALIDATION_URL))
			    .header("Authorization", token).build();

		    @SuppressWarnings("rawtypes")
		    final ResponseEntity<Map> response = restTemplate.exchange(request, Map.class);

		    log.debug(
			    log.isDebugEnabled()
				    ? StringUtils.concatValues("Response Status [",
					    (null == response ? null : response.getStatusCode()), "]")
				    : null);

		    valid = null != response && response.getStatusCode() == HttpStatus.OK;
		}
		catch (final Exception ex) {
		    log.error("##Exception## occurred while validating token", ex);
		}
	    }
	}

	return valid;
    };

}
