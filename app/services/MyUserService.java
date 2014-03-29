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
    public void doSave(Token token) {
        tokens.put(token.uuid, token);
    }

    @Override
    public Identity doFind(IdentityId userId) {
        return users.get(userId.userId() + userId.providerId());
    }

    @Override
    public Token doFindToken(String tokenId) {
        return tokens.get(tokenId);
    }

    @Override
    public Identity doFindByEmailAndProvider(String email, String providerId) {
        Identity result = null;
        for( Identity user : users.values() ) {
            Option<String> optionalEmail = user.email();
            if ( user.identityId().providerId().equals(providerId) &&
                    optionalEmail.isDefined() &&
                    optionalEmail.get().equalsIgnoreCase(email))
            {
                result = user;
                break;
            }
        }
        return result;
    }

    @Override
    public void doDeleteToken(String uuid) {
        tokens.remove(uuid);
    }

    @Override
    public void doDeleteExpiredTokens() {
        Iterator<Map.Entry<String,Token>> iterator = tokens.entrySet().iterator();
        while ( iterator.hasNext() ) {
            Map.Entry<String, Token> entry = iterator.next();
            if ( entry.getValue().isExpired() ) {
                iterator.remove();
            }
        }
    }
}
