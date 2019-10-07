package me.mebubi.myalbum.model;

import java.util.ArrayList;
import java.util.List;

import me.mebubi.myalbum.database.model.Goal;

public class GoalModel {

    private static List<Goal> goals;

    private GoalModel() {

    }

    public synchronized static List<Goal> getGoals() {
        if(goals == null) {
            goals = new ArrayList<>();
            return goals;
        }
        return goals;
    }

    public static void addGoal(Goal goal) {
        synchronized (getGoals()) {
            goals.add(goal);
        }
    }

    public static void deleteGoal(int goalId) {
        synchronized (getGoals()) {
            for (int i = 0; i < goals.size(); i++) {
                if(goals.get(i).getGoalId() == goalId) {
                    GoalModel.getGoals().remove(i);
                }
            }
        }
    }

    public static void clearGoals() {
        synchronized (getGoals()) {
            goals.clear();
        }
    }



}
