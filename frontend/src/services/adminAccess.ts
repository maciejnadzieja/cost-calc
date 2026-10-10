import keycloak from './keycloak'

export const isRealmAdmin = (): boolean =>
  Boolean(keycloak.authenticated && keycloak.hasRealmRole('ADMIN'))
