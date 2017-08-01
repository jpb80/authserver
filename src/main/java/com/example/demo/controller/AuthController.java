package com.example.demo.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import org.joda.time.DateTime;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;


@RestController
@RequestMapping(value="/auth")
public class AuthController {

	private Key key = MacProvider.generateKey();
	
	@RequestMapping(method=RequestMethod.GET, value="/token")
	public String getToken() throws MalformedURLException, IOException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException {

		
		KeyStoreKeyFactory keyStoreKeyFactory = 
			      new KeyStoreKeyFactory(new ClassPathResource("mytest.jks"), "mypass".toCharArray());
		KeyPair kp = keyStoreKeyFactory.getKeyPair("mytest");
		
		DateTime currDate = new DateTime();
		
		String compactJwt = Jwts.builder()
		        .setHeaderParam("kid", 111)
		        .setIssuer("Stormpath")
		        .setSubject("msilverman")
		        .claim("name", "Micah Silverman")
		        .claim("hasMotorcycle", true)
		        .setIssuedAt(currDate.toDate())
		        .setExpiration(currDate.plusMinutes(5).toDate())
		        .signWith(
		            SignatureAlgorithm.RS256,
		            kp.getPrivate()
		        )
		        .compact();
		
		return compactJwt;
	}
	
}
