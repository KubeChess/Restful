# Microchess-Authentication
This microservice handles both requests validation for traffik directed towards the other microservices of the platform, and the actual account 
creation and security management. It's a crucial part of the overall microservices designed.

- 🛡 **Auth Gateway** | Validates JWT tokens on every inbound request via the Kubernetes API
- 🔑 **Auth API** | Exposes `/auth/*` endpoints for user registration, login, password reset, and account deletion

## Architecture
The MicroChess platform is kubernetes native, and as such, its designed
accordigly. The concept is that the `traefik ingress controller` 
(available in K3s by default) is capable of outsourcing authorization 
of a request at route level. The flow of network traffik is herby the following:

```
                            ┌─────────────┐        ┌────────────────┐
                 ┌────────► | ROUTE /auth | ────►  | AUTHENTICATION |
                 │          └─────────────┘        └────────────────┘
            ┌─────────┐            ↑ 
   WEB ────►│ Ingress │            ↓
            └─────────┘  ┌───────────────────┐      
                 │       |  ┌─────────────┐  |     ┌────────────────┐
                 └───────┼► | ROUTE /foo  | ─┼──►  |  SERVICE: foo  |
                 │       |  └─────────────┘  |     └────────────────┘
                 │       |  ┌─────────────┐  |     ┌────────────────┐
                 └───────┼► | ROUTE /bar  | ─┼──►  |  SERVICE: bar  |
                 |       |  └─────────────┘  |     └────────────────┘
                 │       |  ┌─────────────┐  |     ┌────────────────┐
                 └───────┼► | ..........  | ─┼──►  | .............  |
                         |  └─────────────┘  |     └────────────────┘
                         └───────────────────┘
```

## Middleware Configuration
The other MicroChess components that want to use this microservice as a source
of authentication (e.g. every other component) are supposed to implement the 
following middleware and then reference it from the ingress route.

```yaml
apiVersion: traefik.io/v1alpha1
kind: Middleware
metadata:
  name: auth-forward
  namespace: default
spec:
  forwardAuth:
    address: http://authentication.microchess.svc.cluster.local/v1/auth/authorize/native
    trustForwardHeader: true
    authResponseHeaders:
      - X-User-Name
      - X-User-Email
      - X-User-ID
      - X-User-Status
```