package com.skilio.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.skilio.app.R;
import com.skilio.app.models.RoadmapStep;
import java.util.ArrayList;
import java.util.List;

public class StepListAdapter extends RecyclerView.Adapter<StepListAdapter.ViewHolder> {

    public interface OnStepClickListener {
        void onStepClick(RoadmapStep step);
        void onStepComplete(RoadmapStep step);
    }

    private List<RoadmapStep> steps = new ArrayList<>();
    private OnStepClickListener listener;

    public StepListAdapter(OnStepClickListener listener) {
        this.listener = listener;
    }

    public void setSteps(List<RoadmapStep> steps) {
        this.steps = steps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoadmapStep step = steps.get(position);
        holder.tvStepNum.setText("Step " + step.getStepNumber());
        holder.tvTitle.setText(step.getTitle());
        holder.tvDescription.setText(step.getDescription());
        holder.cbComplete.setChecked(step.isCompleted());

        // Lesson badge
        if (step.isLessonGenerated()) {
            holder.tvLessonBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvLessonBadge.setVisibility(View.GONE);
        }

        holder.card.setAlpha(step.isCompleted() ? 0.7f : 1.0f);
        holder.card.setOnClickListener(v -> listener.onStepClick(step));
        holder.cbComplete.setOnClickListener(v -> listener.onStepComplete(step));
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvStepNum, tvTitle, tvDescription, tvLessonBadge;
        CheckBox cbComplete;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            tvStepNum = itemView.findViewById(R.id.tv_step_num);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvLessonBadge = itemView.findViewById(R.id.tv_lesson_badge);
            cbComplete = itemView.findViewById(R.id.cb_complete);
        }
    }
}
