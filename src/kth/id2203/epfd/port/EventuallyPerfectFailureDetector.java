package kth.id2203.epfd.port;



import kth.id2203.epfd.event.Restore;
import kth.id2203.epfd.event.Suspect;
import se.sics.kompics.PortType;

public class EventuallyPerfectFailureDetector extends PortType {
    {
        indication(Suspect.class);
        indication(Restore.class);
    }
}
