package cz.mendelu.xotradov;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.View;
import hudson.widgets.Widget;
import jenkins.model.Jenkins;

import java.util.logging.Logger;

@SuppressWarnings("unused")
@Extension(ordinal=199)
public class SimpleQueueWidget extends Widget {
    private final static Logger logger = Logger.getLogger(SimpleQueueWidget.class.getName());
    public static String getMoveTypeName(){return MoveAction.MOVE_TYPE_PARAM_NAME;}
    public static String getItemIdName(){return MoveAction.ITEM_ID_PARAM_NAME;}
    public Queue.Item[] getItems(){
        return Jenkins.get().getQueue().getItems();
    }
    public boolean isFilterQueue(){
        return false;
    }
}
