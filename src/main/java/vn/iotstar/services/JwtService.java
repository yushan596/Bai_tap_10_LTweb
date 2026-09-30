package vn.iotstar.services;

import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import vn.iotstar.exception.ExpiredJwtException;
import vn.iotstar.exception.InvalidJwtException;

/**
 * Sinh / xac thuc JWT bang Nimbus JOSE + JWT (HS256).
 * Cung API voi JwtService dung JJWT trong bai giang.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    // ---------- Doc claims ----------
    public String extractUsername(String token) {
        return extractClaim(token, JWTClaimsSet::getSubject);
    }

    public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, JWTClaimsSet::getExpirationTime);
    }

    // ---------- Sinh token ----------
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        try {
            Date now = new Date();
            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(now)
                    .expirationTime(new Date(now.getTime() + expiration))
                    .jwtID(UUID.randomUUID().toString());
            extraClaims.forEach(claims::claim);

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims.build());
            jwt.sign(new MACSigner(getSignInKey()));
            return jwt.serialize(); // header.payload.signature
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot sign JWT", e);
        }
    }

    // ---------- Kiem tra token ----------
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // da verify chu ky + het han
        return username.equals(userDetails.getUsername());
    }

    /** Parse + kiem tra alg + verify chu ky + kiem tra exp. */
    private JWTClaimsSet extractAllClaims(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
                throw new InvalidJwtException("Unsupported JWT algorithm");
            }
            if (!jwt.verify(new MACVerifier(getSignInKey()))) {
                throw new InvalidJwtException("The JWT signature is invalid");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                throw new ExpiredJwtException("The JWT token has expired");
            }
            return claims;
        } catch (ParseException e) {
            throw new InvalidJwtException("Invalid compact JWT string", e);
        } catch (JOSEException e) {
            throw new InvalidJwtException("Cannot verify JWT", e);
        }
    }

    private byte[] getSignInKey() {
        return Base64.getDecoder().decode(secretKey); // >= 32 byte cho HS256
    }
}
