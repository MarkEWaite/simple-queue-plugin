package cz.mendelu.xotradov.test;

import cz.mendelu.xotradov.MoveAction;
import cz.mendelu.xotradov.SimpleQueueWidget;
import hudson.model.*;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskFuture;
import hudson.search.Search;
import hudson.search.SearchIndex;
import hudson.security.ACL;
import hudson.widgets.Widget;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SleepBuilder;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class BasicTest {
    public static Logger logger = Logger.getLogger(BasicTest.class.getName());
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    private TestHelper helper = new TestHelper(jenkinsRule);

    @Test
    public void widgetTest() throws Exception {
        Widget widget = jenkinsRule.jenkins.getWidgets().get(0);
        assertTrue(widget instanceof SimpleQueueWidget);
    }
    @Test
    public void oneBuildSuccessTest() throws Exception {
        FreeStyleProject projectA = helper.createProject("projectA",1000);
        QueueTaskFuture futureA = helper.schedule(projectA);
        while (!Queue.getInstance().getBuildableItems().isEmpty()) {
            Thread.sleep(10);
        }
        jenkinsRule.assertBuildStatusSuccess(futureA);
    }

    @Test
    public void twoItemsUpperDownTest() throws Exception {
        helper.fillQueueFor(20000);
        Queue queue = Queue.getInstance();
        //now can be queue filled predictably
        FreeStyleProject projectC = helper.createProject("projectC", 20000);
        QueueTaskFuture futureC = helper.schedule(projectC);
        FreeStyleProject projectD = helper.createProject("projectD",20000);
        QueueTaskFuture futureD = helper.schedule(projectD);
        while (queue.getBuildableItems().size() != 2){
            Thread.sleep(5);
        }
        assertEquals(projectD.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectC.getDisplayName(),queue.getItems()[1].task.getDisplayName());
        assertTrue(jenkinsRule.jenkins.hasPermission(Jenkins.ADMINISTER));
        assertEquals(2,queue.getBuildableItems().size());
        assertEquals(2,queue.getItems().length);
        MoveAction moveAction = (MoveAction)jenkinsRule.jenkins.getActions().get(1);
        moveAction.moveDown(queue.getItems()[0],queue);
        queue.maintain();
        assertEquals(projectC.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectD.getDisplayName(),queue.getItems()[1].task.getDisplayName());
    }

    @Test
    public void twoItemsLowerUpTest() throws Exception {
        helper.fillQueueFor(20000);
        Queue queue = Queue.getInstance();
        //now can be queue filled predictably
        FreeStyleProject projectC = helper.createProject("projectC", 20000);
        QueueTaskFuture futureC = helper.schedule(projectC);
        FreeStyleProject projectD = helper.createProject("projectD",20000);
        QueueTaskFuture futureD = helper.schedule(projectD);
        while (queue.getBuildableItems().size() != 2){
            Thread.sleep(5);
        }
        assertEquals(projectD.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectC.getDisplayName(),queue.getItems()[1].task.getDisplayName());
        assertTrue(jenkinsRule.jenkins.hasPermission(Jenkins.ADMINISTER));
        assertEquals(2,queue.getBuildableItems().size());
        assertEquals(2,queue.getItems().length);
        MoveAction moveAction = (MoveAction)jenkinsRule.jenkins.getActions().get(1);
        moveAction.moveUp(queue.getItems()[1],queue);
        queue.maintain();
        assertEquals(projectC.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectD.getDisplayName(),queue.getItems()[1].task.getDisplayName());
    }

    @Test
    public void backAndForthTest() throws Exception {
        helper.fillQueueFor(20000);
        Queue queue = Queue.getInstance();
        //now can be queue filled predictably
        FreeStyleProject projectC = helper.createProject("projectC", 20000);
        QueueTaskFuture futureC = helper.schedule(projectC);
        FreeStyleProject projectD = helper.createProject("projectD",20000);
        QueueTaskFuture futureD = helper.schedule(projectD);
        while (queue.getBuildableItems().size() != 2){
            Thread.sleep(5);
        }
        assertEquals(projectD.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectC.getDisplayName(),queue.getItems()[1].task.getDisplayName());
        assertTrue(jenkinsRule.jenkins.hasPermission(Jenkins.ADMINISTER));
        assertEquals(2,queue.getBuildableItems().size());
        assertEquals(2,queue.getItems().length);
        MoveAction moveAction = (MoveAction)jenkinsRule.jenkins.getActions().get(1);
        moveAction.moveUp(queue.getItems()[1],queue);
        queue.maintain();
        assertEquals(projectC.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectD.getDisplayName(),queue.getItems()[1].task.getDisplayName());
        moveAction.moveDown(queue.getItems()[0],queue);
        queue.maintain();
        assertEquals(projectD.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectC.getDisplayName(),queue.getItems()[1].task.getDisplayName());
    }
    @Test
    public void fourCUpUp() throws Exception {
        helper.fillQueueFor(25000);
        FreeStyleProject projectC = helper.createAndSchedule("projectC",25000);
        FreeStyleProject projectD = helper.createAndSchedule("projectD",25000);
        FreeStyleProject projectE = helper.createAndSchedule("projectE",25000);
        FreeStyleProject projectF = helper.createAndSchedule("projectF",25000);
        Queue queue = Queue.getInstance();
        assertEquals(4,queue.getBuildableItems().size());
        MoveAction moveAction = (MoveAction)jenkinsRule.jenkins.getActions().get(1);
        assertEquals(projectC.getDisplayName(),queue.getItems()[3].task.getDisplayName());
        moveAction.moveUp(queue.getItems()[3],queue);
        Thread.sleep(5000);
        assertEquals(projectC.getDisplayName(),queue.getItems()[2].task.getDisplayName());
        moveAction.moveUp(queue.getItems()[2],queue);
        Thread.sleep(5000);
        assertEquals(projectF.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectC.getDisplayName(),queue.getItems()[1].task.getDisplayName());
        assertEquals(projectE.getDisplayName(),queue.getItems()[2].task.getDisplayName());
        assertEquals(projectD.getDisplayName(),queue.getItems()[3].task.getDisplayName());
    }
    @Test
    public void fourDUpUpEDown() throws Exception {
        helper.fillQueueFor(30000);
        FreeStyleProject projectC = helper.createAndSchedule("projectC",25000);
        FreeStyleProject projectD = helper.createAndSchedule("projectD",25000);
        FreeStyleProject projectE = helper.createAndSchedule("projectE",25000);
        FreeStyleProject projectF = helper.createAndSchedule("projectF",25000);
        Queue queue = Queue.getInstance();
        MoveAction moveAction = (MoveAction)jenkinsRule.jenkins.getActions().get(1);
        assertEquals(projectD.getDisplayName(),queue.getItems()[2].task.getDisplayName());
        moveAction.moveUp(queue.getItems()[2],queue);//D
        queue.maintain();
        assertEquals(projectD.getDisplayName(),queue.getItems()[1].task.getDisplayName());
        moveAction.moveUp(queue.getItems()[1],queue);//D
        queue.maintain();
        assertEquals(projectE.getDisplayName(),queue.getItems()[2].task.getDisplayName());
        moveAction.moveDown(queue.getItems()[2],queue);//E
        queue.maintain();
        assertEquals(projectD.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectF.getDisplayName(),queue.getItems()[1].task.getDisplayName());
        assertEquals(projectC.getDisplayName(),queue.getItems()[2].task.getDisplayName());
        assertEquals(projectE.getDisplayName(),queue.getItems()[3].task.getDisplayName());
    }
    @Test
    public void fourFDownDDownCUpUpFDown() throws Exception {
        helper.fillQueueFor(35000);
        FreeStyleProject projectC = helper.createAndSchedule("projectC",35000);
        FreeStyleProject projectD = helper.createAndSchedule("projectD",35000);
        FreeStyleProject projectE = helper.createAndSchedule("projectE",35000);
        FreeStyleProject projectF = helper.createAndSchedule("projectF",35000);
        Queue queue = Queue.getInstance();
        MoveAction moveAction = (MoveAction)jenkinsRule.jenkins.getActions().get(1);
        assertEquals(projectF.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        moveAction.moveDown(queue.getItems()[0],queue);//F
        queue.maintain();
        assertEquals(projectD.getDisplayName(),queue.getItems()[2].task.getDisplayName());
        moveAction.moveDown(queue.getItems()[2],queue);//D
        queue.maintain();
        assertEquals(projectC.getDisplayName(),queue.getItems()[2].task.getDisplayName());
        moveAction.moveUp(queue.getItems()[2],queue);//C
        queue.maintain();
        assertEquals(projectC.getDisplayName(),queue.getItems()[1].task.getDisplayName());
        moveAction.moveUp(queue.getItems()[1],queue);//C
        queue.maintain();
        assertEquals(projectF.getDisplayName(),queue.getItems()[2].task.getDisplayName());
        moveAction.moveDown(queue.getItems()[2],queue);//F
        queue.maintain();
        assertEquals(projectC.getDisplayName(),queue.getItems()[0].task.getDisplayName());
        assertEquals(projectE.getDisplayName(),queue.getItems()[1].task.getDisplayName());
        assertEquals(projectD.getDisplayName(),queue.getItems()[2].task.getDisplayName());
        assertEquals(projectF.getDisplayName(),queue.getItems()[3].task.getDisplayName());
    }
}
