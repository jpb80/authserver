package com.example.demo.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.PublicCreds;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.lang.Strings;


@RestController
@RequestMapping(value="/auth")
public class AuthController {

	private Map<String, PublicKey> publicKeys = new HashMap<>();
	
	@RequestMapping(method=RequestMethod.GET, value="/token")
	public String getToken() throws MalformedURLException, IOException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException {
		
		KeyStoreKeyFactory keyStoreKeyFactory = 
			      new KeyStoreKeyFactory(new ClassPathResource("mytest.jks"), "mypass".toCharArray());
		KeyPair kp = keyStoreKeyFactory.getKeyPair("mytest");
		PublicKey publicKey = kp.getPublic();
		UUID kid = UUID.randomUUID();
		PublicCreds publicCreds = new PublicCreds(kid.toString(), TextCodec.BASE64.encode(publicKey.getEncoded()));
		
		DateTime currDate = new DateTime();
		
		String compactJwt = Jwts.builder()
		        .setHeaderParam("kid", kid)
		        .setIssuer("jwt-authserver")
		        .setSubject("jwt")
		        .claim("username", "jpb@test.com")
		        .claim("name", "jordan")
		        .setIssuedAt(currDate.toDate())
		        .setExpiration(currDate.plusMinutes(5).toDate())
		        .signWith(
		            SignatureAlgorithm.RS256,
		            kp.getPrivate()
		        )
		        .compact();
		
		return compactJwt;
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/validateToken")
	public String validateAuthToken(@RequestParam String jwt) {
		
		Jws<Claims> jwsClaims = Jwts.parser()
				.setSigningKeyResolver(getSigningKeyResolver())
				.parseClaimsJws(jwt);
		
		return jwsClaims == null ? "" : jwsClaims.toString();
	}
	
	private SigningKeyResolver signingKeyResolver = new SigningKeyResolverAdapter() {	
		@Override
		public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
			String kid = jwsHeader.getKeyId();
			if (!Strings.hasText(kid)) {
				throw new JwtException("Invalid key");
			}
			Key key = publicKeys.get(kid);
			if (key == null) {
				throw new JwtException("missing key");
			}
			return key;
		}
	};
	
	public SigningKeyResolver getSigningKeyResolver() {
		return signingKeyResolver;
	}
	
}
