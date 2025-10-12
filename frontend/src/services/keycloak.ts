import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:9090',
  realm: 'cost-calc-realm',
  clientId: 'cost-calc-client'
});

export default keycloak;
