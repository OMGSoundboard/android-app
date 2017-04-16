package audio.omgsoundboard.android;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * An adapter for Sounds
 */
public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.ViewHolder> {
    private ArrayList<Sound> sounds;
    private boolean shouldShowFavsOnly = false;

    public SoundAdapter(ArrayList<Sound> soundArray) {
        sounds = soundArray;
    }

    public void onlyShowFavorites() {
        shouldShowFavsOnly = true;
        for (Sound sound : new ArrayList<>(sounds)) {
            if (!sound.getFavorite()) {
                notifyItemRemoved(sounds.indexOf(sound));
                sounds.remove(sound);
            }
        }
    }

    public void showAllSounds(Context context) {
        shouldShowFavsOnly = false;
        sounds = SoundStore.getAllSounds(context);
        notifyDataSetChanged();
    }

    @Override
    public SoundAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.title.setText(sounds.get(position).getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onEvent(String done) {
                holder.setNormalColors();
                EventBus.getDefault().unregister(this);
            }

            @Override
            public void onClick(View view) {
                holder.setPlayingColors();
                if (EventBus.getDefault().isRegistered(this)) {
                    return;
                }
                EventBus.getDefault().register(this);
                EventBus.getDefault().post(sounds.get(holder.getAdapterPosition()));
            }
        });

        boolean isFavorite = sounds.get(position).getFavorite();
        holder.favButton.setImageResource(isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable
                .ic_favorite_outline_white_24dp);

        holder.favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newFavStatus = !sounds.get(holder.getAdapterPosition()).getFavorite();
                sounds.get(holder.getAdapterPosition()).setFavorite(newFavStatus);
                if (newFavStatus) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_favorite_white_24dp);
                    v.setContentDescription(v.getContext().getString(R.string.fav_desc));
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_favorite_outline_white_24dp);
                    v.setContentDescription(v.getContext().getString(R.string.not_fav_desc));
                }
                if (shouldShowFavsOnly) {
                    // Remove from the list.
                    sounds.remove(sounds.get(holder.getAdapterPosition()));
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return sounds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageButton favButton;
        private int accentColor;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            favButton = (ImageButton) v.findViewById(R.id.fav_button);
            accentColor = itemView.getContext().getResources().getColor(R.color.colorAccent);
        }

        public void setNormalColors() {
            ((CardView) itemView).setCardBackgroundColor(accentColor);
            title.setTextColor(Color.WHITE);
            favButton.clearColorFilter();
        }

        public void setPlayingColors() {
            ((CardView) itemView).setCardBackgroundColor(Color.WHITE);
            title.setTextColor(accentColor);
            favButton.setColorFilter(accentColor);
        }
    }
}
