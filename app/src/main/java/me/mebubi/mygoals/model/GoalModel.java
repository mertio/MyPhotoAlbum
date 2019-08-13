package me.mebubi.mygoals.model;

import java.util.ArrayList;
import java.util.List;

import me.mebubi.mygoals.database.model.Goal;

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
        synchronized (goals) {
            goals.add(goal);
        }
    }

    public static void deleteGoal(int goalId) {
        synchronized (goals) {
            for (int i = 0; i < goals.size(); i++) {
                if(goals.get(i).getGoalId() == goalId) {
                    GoalModel.getGoals().remove(i);
                }
            }
        }
    }

    public static void clearGoals() {
        synchronized (goals) {
            goals.clear();
        }
    }



}