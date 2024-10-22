package com.example.cats_catch_mice;

import java.util.HashMap;
import java.util.Map;

public class RoomData {

    private String ownerId;
    private Map<String, Object> members;

    public RoomData(String ownerId) {
        this.ownerId = ownerId;
        this.members = new HashMap<>();
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Map<String, Object> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Object> members) {
        this.members = members;
    }

    public void addMember(String userId, Map<String, Object> memberData) {
        this.members.put(userId, memberData);
    }

    // When a member leaves the room, remove the member from the room
    public void removeMember(String userId) {
        this.members.remove(userId);
    }


    // get the information of one member
    public Object getMemberData(String userId) {
        return this.members.get(userId);
    }

    // update the information of one member
    public void updateMemberData(String userId, Map<String, Object> memberData) {
        this.members.put(userId, memberData);
    }

    // TODO: 需要详细的item信息才能做更细化的update function

}
