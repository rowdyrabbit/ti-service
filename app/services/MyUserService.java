package services;

import play.Application;
import scala.Option;
import securesocial.core.Identity;
import securesocial.core.IdentityId;
import securesocial.core.java.BaseUserService;

import securesocial.core.java.Token;

import java.util.*;

/**
 * A Sample In Memory user service in Java
 *
 * Note: This is NOT suitable for a production environment and is provided only as a guide.
 * A real implementation would persist things in a database
 */
public class MyUserService extends BaseUserService {
    private HashMap<String, Identity> users  = new HashMap<String, Identity>();
    private HashMap<String, Token> tokens = new HashMap<String, Token>();

    public MyUserService(Application application) {
        super(application);
    }

    @Override
    public Identity doSave(Identity user) {
        users.put(user.identityId().userId() + user.identityId().providerId(), user);
        // this sample returns the same user object, but you could return an instance of your own class
        // here as long as it implements the Identity interface. This will allow you to use your own class in the
        // protected actions and event callbacks. The same goes for the doFind(UserId userId) method.
        return user;
    }

    @Override
    public Identity doFind(IdentityId userId) {
        return users.get(userId.userId() + userId.providerId());
    }





    /** The following methods aren't used as we are not working with tokens, only identities which are provided by third parties. Therefore they have empty implementations. **/
    @Override
    public void doSave(Token token) {
        //Not using a UsernamePassword provider.
    }

    @Override
    public Token doFindToken(String tokenId) {
        //Not using a UsernamePassword provider.
        return null;
    }

    @Override
    public Identity doFindByEmailAndProvider(String email, String providerId) {
        //Not using a UsernamePassword provider.
        return null;
    }

    @Override
    public void doDeleteToken(String uuid) {
        //Not using a UsernamePassword provider.
    }

    @Override
    public void doDeleteExpiredTokens() {
        //Not using a UsernamePassword provider.
    }
}
