package cz.mendelu.xotradov;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.RootAction;
import hudson.model.queue.QueueSorter;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Jaroslav Otradovec
 */
@SuppressWarnings("unused")
@Extension
public class MoveAction implements RootAction {
    private static Logger logger = Logger.getLogger(MoveAction.class.getName());
    public static final String MOVE_TYPE_PARAM_NAME= "moveType";
    public static final String ITEM_ID_PARAM_NAME="itemId";
    private boolean isSorterSet=false;
    @CheckForNull
    @Override
    public String getIconFileName() {
            return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "simpleMove";
    }

    public void doMove(final StaplerRequest request, final StaplerResponse response) {
        Jenkins j;
        if ((j = Jenkins.getInstanceOrNull()) != null) {
            Queue queue = j.getQueue();
            if (queue != null) {
                try {
                    Queue.Item item = queue.getItem(Long.parseLong(request.getParameter(ITEM_ID_PARAM_NAME)));
                    MoveType moveType = MoveType.valueOf(request.getParameter(MOVE_TYPE_PARAM_NAME));
                    if (item != null){
                        switch (moveType){
                            case UP:
                                moveUp(item,queue);
                                break;
                            case DOWN:
                                moveDown(item,queue);
                                break;
                        }
                    }
                }catch (NumberFormatException nfe){
                    logger.info("Wrong item id");
                }catch (IllegalArgumentException iae){
                    logger.info("Wrong move type");
                }
            }
        }
        try {
            response.sendRedirect2(request.getRootPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void moveDown(Queue.Item itemA, Queue queue) {
        Queue.Item[] items = queue.getItems();
        for (Queue.Item item: items) logger.info(String.valueOf(item.getId())+"  "+String.valueOf(item.isBlocked()));
        Queue.Item itemB = getItemAfter(itemA, items);
        if (itemB!=null){
            if (!isSorterSet){
                setSorter(queue);
            }
            QueueSorter queueSorter = queue.getSorter();
            if (queueSorter instanceof SimpleQueueSorter){
                ((SimpleQueueSorter) queueSorter).getSimpleQueueComparator().addDesire(itemA.getId(),itemB.getId());
                resort(queue);
            }
        }
    }

    private Queue.Item getItemAfter(Queue.Item itemA, Queue.Item[] items) {
        if (items.length >= 2) {
            Queue.Item previous = null;
            for (Queue.Item itemB : items) {
                if ((previous!=null) && (previous.getId() == itemA.getId())) {
                    return itemB;
                }
                previous=itemB;
            }
        }
        return null;
    }

    private void setSorter(Queue queue) {
        if (!isSorterSet){
            QueueSorter originalQueueSorter = queue.getSorter();
            SimpleQueueSorter simpleQueueSorter = new SimpleQueueSorter(originalQueueSorter);
            queue.setSorter(simpleQueueSorter);
            isSorterSet=true;
        }
    }

    private Queue.Item getItemBefore(Queue.Item itemA, Queue.Item[] items) {
        if (items.length >= 2) {
            Queue.Item itemB = null;
            for (Queue.Item itemFor : items) {
                if (itemFor.getId() == itemA.getId()) {
                    return itemB;
                }
                itemB=itemFor;
            }
        }
        return null;
    }

    private void resort(Queue queue) {
        queue.getSorter().sortBuildableItems(queue.getBuildableItems());
    }

    private void moveUp(Queue.Item itemA, Queue queue) {
        Queue.Item[] items = queue.getItems();
        Queue.Item itemB = getItemBefore(itemA, items);
        if (itemB!=null){
            if (!isSorterSet){
                setSorter(queue);
            }
            QueueSorter queueSorter = queue.getSorter();
            if (queueSorter instanceof SimpleQueueSorter){
                ((SimpleQueueSorter) queueSorter).getSimpleQueueComparator().addDesire(itemB.getId(),itemA.getId());
                resort(queue);
            }
        }
    }

}
