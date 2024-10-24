package com.example.cats_catch_mice;

import java.util.HashMap;
import java.util.Map;

public class RoomData {

     /*
    sync data to firebase
    这里只需要写入数据的子节点 rooms的数据进入database
    数据格式如下：

    "rooms": {
    "roomId12345": {
      "owner": "UUID12345",
      "members": {
        "UUID12345": {
          "lat": 37.7749,
          "lng": -122.4194,
          "item1": 0,
          "item2": 1
        },
        "UUID67890": {
          "lat": 37.7750,
          "lng": -122.4200,
          "item1": 2,
          "item2": 3
        }
      }
    }
    */


    private String ownerId;
    private Map<String, Object> members;

    public RoomData(String ownerId) {
        this.ownerId = ownerId;

        // Members should record all the members in the room include the owner
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
