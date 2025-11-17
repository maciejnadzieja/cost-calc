import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID
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
