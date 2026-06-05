package com.skilio.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skilio.app.R;
import com.skilio.app.models.Roadmap;
import com.skilio.app.models.RoadmapStep;
import java.util.ArrayList;
import java.util.List;

public class RoadmapListAdapter extends RecyclerView.Adapter<RoadmapListAdapter.ViewHolder> {

    public interface OnRoadmapClickListener {
        void onRoadmapClick(Roadmap roadmap);
        void onRoadmapDelete(Roadmap roadmap);
    }

    private List<Roadmap> roadmaps = new ArrayList<>();
    private OnRoadmapClickListener listener;

    public RoadmapListAdapter(OnRoadmapClickListener listener) {
        this.listener = listener;
    }

    public void setRoadmaps(List<Roadmap> roadmaps) {
        this.roadmaps = roadmaps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_roadmap, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Roadmap roadmap = roadmaps.get(position);
        holder.tvTitle.setText(roadmap.getTitle());
        holder.tvSubtitle.setText(roadmap.getSubtitle());

        int total = roadmap.getSteps().size();
        long completed = roadmap.getSteps().stream().filter(RoadmapStep::isCompleted).count();
        long lessonsGenerated = roadmap.getSteps().stream().filter(RoadmapStep::isLessonGenerated).count();
        holder.tvMeta.setText(total + " steps  •  " + completed + " done  •  " + lessonsGenerated + " lessons");

        holder.itemView.setOnClickListener(v -> listener.onRoadmapClick(roadmap));
        holder.btnDelete.setOnClickListener(v -> listener.onRoadmapDelete(roadmap));
    }

    @Override
    public int getItemCount() {
        return roadmaps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvMeta;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvMeta = itemView.findViewById(R.id.tv_meta);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
