$ grpcurl -plaintext  -d '{\"userId\":1234567890,\"host\":\"localhost\",\"port\":1331}' localhost:1990 owl.session.SessionMapperService/CreateSessionRequest
Error invoking method "owl.session.SessionMapperService/CreateSessionRequest": failed to query for serviceName descriptor "owl.session.SessionMapperService": server does not support the reflection API
