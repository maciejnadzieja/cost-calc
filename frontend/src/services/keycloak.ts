import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:9090',
  realm: 'cost-calc-realm',
  clientId: 'cost-calc-client'
});

export const getKeycloak = () => keycloak;

export const getToken = () => {
  if (keycloak.authenticated) {
    return keycloak.token;
  }
  return null;
};

export const getUserId = () => {
  if (keycloak.authenticated) {
    return keycloak.subject;
  }
  return null;
};

export default keycloak;
