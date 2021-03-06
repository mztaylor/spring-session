package sample

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.servlet.http.HttpServletResponse

/**
 * Ensures that Spring Security and Session are working
 *
 * @author Rob Winch
 */
@Stepwise
class RestTests extends Specification {

    @Shared
    RESTClient client = new RESTClient(System.properties.'geb.build.baseUrl')

    @Shared
    String session

    def 'Unauthenticated user sent to log in page'() {
        when: 'unauthenticated user request protected page'
        def resp = client.get path: '/', headers: ['Accept':'application/json']
        then: 'sent to the log in page'
        def e = thrown(HttpResponseException)
        e.response.status == HttpServletResponse.SC_UNAUTHORIZED
    }

    def 'Authenticate with Basic Works'() {
        when: 'Authenticate with Basic'
        def username, response
        client.get(path: '/', headers: ['Authorization': 'Basic ' + 'user:password'.bytes.encodeBase64() ]) { resp, json ->
            response = resp
            username = json.username
            session = resp.headers.'x-auth-token'
        }
        then: 'Access the User information and obtain session via x-auth-token header'
        response.status == HttpServletResponse.SC_OK
        username == 'user'
        session
    }

    def 'Authenticate with x-auth-token works'() {
        when: 'Authenticate with x-auth-token'
        def username, response
        client.get(path: '/', headers: ['x-auth-token': session ]) { resp, json ->
            response = resp
            username = json.username
        }
        then: 'Access the User information'
        response.status == HttpServletResponse.SC_OK
        username == 'user'
    }

    def 'Logout'() {
        when: 'invalide session'
        def response
        client.get(path: '/logout', headers: ['x-auth-token': session ]) { resp, json ->
            response = resp
            session = resp.headers.'x-auth-token'
        }
        then: 'The session is deleted and an empty x-auth-token is returned'
        response.status == HttpServletResponse.SC_NO_CONTENT
        session == ''
    }
}