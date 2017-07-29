package cn.esoule.distributed.terminal.event.listener;

import java.util.List;
import cn.esoule.distributed.terminal.event.core.EventListener;

/**
 * 
 * @author CaoXin
 */
public interface IGameEventListener extends EventListener {

    public List<String> getEvents();
}