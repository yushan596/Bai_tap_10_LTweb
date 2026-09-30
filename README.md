# JWT Spring Boot 3 + Security 6 (Nimbus JOSE + JWT)

## Chạy
1. MySQL chạy ở localhost:3306 (DB `jwt_springboot3` tự tạo nhờ `createDatabaseIfNotExist=true`).
2. Đặt biến môi trường `DB_USER`, `DB_PASSWORD` (mặc định root/root) hoặc sửa application.properties.
3. `mvn spring-boot:run` → http://localhost:8005/login

## Test bằng Postman
1. POST /auth/signup  {"email":"trungnh@hcmute.edu.vn","password":"123456","fullName":"Nguyễn Hữu Trung"}
2. POST /auth/login   {"email":"trungnh@hcmute.edu.vn","password":"123456"} → lấy `token`
3. GET /users/me không token → 403
4. GET /users/me + Authorization: Bearer <token> → 200
5. Token sai định dạng / sai chữ ký / hết hạn → 401 (ProblemDetail JSON)

## Đổi JJWT → Nimbus
| JJWT | Nimbus |
|---|---|
| Jwts.builder()...signWith(key, HS256).compact() | new SignedJWT(header, claims); jwt.sign(new MACSigner(key)); jwt.serialize() |
| Jwts.parser().verifyWith(key).build().parseSignedClaims(t).getPayload() | SignedJWT.parse(t); jwt.verify(new MACVerifier(key)); jwt.getJWTClaimsSet() |
| Claims::getSubject / Claims::getExpiration | JWTClaimsSet::getSubject / getExpirationTime |
| SignatureException, ExpiredJwtException (thư viện tự ném) | Tự kiểm tra chữ ký/exp → InvalidJwtException / ExpiredJwtException |
