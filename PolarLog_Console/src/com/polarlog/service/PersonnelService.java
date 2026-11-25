package com.polarlog.service;

import com.polarlog.dao.DataStore;
import com.polarlog.exception.ItemNotFoundException;
import com.polarlog.model.CommandOperator;
import com.polarlog.model.MovementLog;
import com.polarlog.model.Person;
import com.polarlog.model.StationMember;
import com.polarlog.model.SyncRecord;
import com.polarlog.sync.SyncQueue;
import com.polarlog.util.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** People, check-in and check-out, and the movement log. */
public class PersonnelService {
    private final DataStore store;
    private final SyncQueue syncQueue;
    private final ActivityLog log;

    public PersonnelService(DataStore store, SyncQueue syncQueue, ActivityLog log) {
        this.store = store;
        this.syncQueue = syncQueue;
        this.log = log;
    }

    public StationMember addMember(String name, String station) {
        String cleanName = Validator.requireName(name);
        String cleanStation = Validator.requireName(station);
        StationMember member = new StationMember(store.getPeople().nextId(), cleanName, cleanStation);
        store.getPeople().add(member);
        syncQueue.add("MEMBER_ADDED", member.getDetails(), SyncRecord.NORMAL);
        log.record("Added station member " + cleanName);
        return member;
    }

    public CommandOperator addOperator(String name, String station) {
        String cleanName = Validator.requireName(name);
        String cleanStation = Validator.requireName(station);
        CommandOperator operator = new CommandOperator(store.getPeople().nextId(), cleanName, cleanStation);
        store.getPeople().add(operator);
        syncQueue.add("OPERATOR_ADDED", operator.getDetails(), SyncRecord.NORMAL);
        log.record("Added command operator " + cleanName);
        return operator;
    }

    public StationMember getMember(int id) throws ItemNotFoundException {
        Person person = store.getPeople().get(id);
        if (!(person instanceof StationMember)) {
            throw new ItemNotFoundException("ID " + id + " is not a station member");
        }
        return (StationMember) person;
    }

    public CommandOperator getOperator(int id) throws ItemNotFoundException {
        Person person = store.getPeople().get(id);
        if (!(person instanceof CommandOperator)) {
            throw new ItemNotFoundException("ID " + id + " is not a command centre operator");
        }
        return (CommandOperator) person;
    }

    public void checkIn(int id, String location) throws ItemNotFoundException {
        String cleanLocation = Validator.requireLocation(location);
        StationMember member = getMember(id);
        member.checkIn(cleanLocation);
        store.getMovementLog().add(new MovementLog(member.getName(), "CHECK_IN", cleanLocation));
        syncQueue.add("CHECK_IN", member.getName() + " at " + cleanLocation, SyncRecord.NORMAL);
        log.record(member.getName() + " checked in at " + cleanLocation);
    }

    public void checkOut(int id) throws ItemNotFoundException {
        StationMember member = getMember(id);
        String place = member.getCurrentLocation();
        member.checkOut();
        store.getMovementLog().add(new MovementLog(member.getName(), "CHECK_OUT", place));
        syncQueue.add("CHECK_OUT", member.getName() + " from " + place, SyncRecord.NORMAL);
        log.record(member.getName() + " checked out from " + place);
    }

    public List<Person> getAllPeople() {
        return store.getPeople().getAll();
    }

    public List<MovementLog> getMovementLog() {
        return new ArrayList<>(store.getMovementLog());
    }

    /** Sorted names of members who are currently checked in. */
    public Set<String> getCheckedInNames() {
        Set<String> names = new TreeSet<>();
        for (Person person : store.getPeople().getAll()) {
            if (person instanceof StationMember && ((StationMember) person).isCheckedIn()) {
                names.add(person.getName());
            }
        }
        return names;
    }
}
