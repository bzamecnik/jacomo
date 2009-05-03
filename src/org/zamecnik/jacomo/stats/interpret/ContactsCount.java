package org.zamecnik.jacomo.stats.interpret;

import org.zamecnik.jacomo.stats.*;

/**
 *
 * @author Bohouš
 */
public class ContactsCount extends Interpreter {
    public ContactsCount(PresenceManager presenceManager) {
        this.presenceManager = presenceManager;
    }

    @Override
    public Interpreter.Result interpret() {
        return new Result(presenceManager.getContacts().size());
    }
        
    public class Result extends Interpreter.Result {
        public Result(int contactsCount) {
            this.contactsCount = contactsCount;
        }

        public int getContactsCount() {
            return contactsCount;
        }

        int contactsCount;
    }

}
