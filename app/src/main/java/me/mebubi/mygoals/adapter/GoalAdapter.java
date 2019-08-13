package me.mebubi.mygoals.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.mebubi.mygoals.database.model.Goal;
import me.mebubi.mygoals.view.GoalView;

public class GoalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Goal> goalList;
    private Context context;

    public GoalAdapter(List<Goal> goalList, Context context) {
        this.goalList = goalList;
        this.context = context;
    }

    public List<Goal> getGoalList() {
        return goalList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        GoalViewHolder goalViewHolder;
        GoalView goalView = new GoalView(viewGroup.getContext());
        goalViewHolder = new GoalViewHolder(goalView);

        return goalViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ((GoalViewHolder) viewHolder).getGoalView().init(goalList.get(i));

    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }


    private class GoalViewHolder extends RecyclerView.ViewHolder {

        private GoalView goalView;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalView = (GoalView) itemView;
        }

        public GoalView getGoalView() {
            return goalView;
        }

    }


}
