package net.ssignal.protocols;

public class Group_Small extends Group {

    public byte 编号;
    public String 备注;
    public Contact 群主;
    public boolean 待加入确认 = false;
    public GroupMember 群成员[];

}
