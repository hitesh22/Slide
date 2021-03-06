package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.List;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.ImgurAlbum.Image;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.util.SubmissionParser;

public class AlbumView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Image> users;

    private final Activity main;

    public boolean paddingBottom;
    public int height;

    public AlbumView(final Activity context, final List<Image> users, int height) {

        this.height = height;
        main = context;
        this.users = users;

        paddingBottom = main.findViewById(R.id.toolbar) == null;
        if (context.findViewById(R.id.grid) != null)
            context.findViewById(R.id.grid).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater l = context.getLayoutInflater();
                    View body = l.inflate(R.layout.album_grid_dialog, null, false);
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(context);
                    GridView gridview = (GridView) body.findViewById(R.id.images);
                    gridview.setAdapter(new ImageGridAdapter(context, users));


                    b.setView(body);
                    final Dialog d = b.create();
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            if (context instanceof Album) {
                                ((LinearLayoutManager) ((Album) context).album.album.recyclerView.getLayoutManager()).scrollToPositionWithOffset(position + 1, context.findViewById(R.id.toolbar).getHeight());


                            } else {
                                ((LinearLayoutManager) ((RecyclerView) context.findViewById(R.id.images)).getLayoutManager()).scrollToPositionWithOffset(position + 1, context.findViewById(R.id.toolbar).getHeight());
                            }
                            d.dismiss();
                        }
                    });
                    d.show();
                }
            });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_image, parent, false);
            return new AlbumViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.spacer, parent, false);
            return new SpacerViewHolder(v);
        }
    }

    public double getHeightFromAspectRatio(int imageHeight, int imageWidth, int viewWidth) {
        double ratio = (double) imageHeight / (double) imageWidth;
        return (viewWidth * ratio);

    }

    @Override
    public int getItemViewType(int position) {
        int SPACER = 6;
        if (!paddingBottom && position == 0) {
            return SPACER;
        } else if (paddingBottom && position == getItemCount() - 1) {
            return SPACER;
        } else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder2, int i) {
        if (holder2 instanceof AlbumViewHolder) {
            final int position = paddingBottom ? i : i - 1;

            AlbumViewHolder holder = (AlbumViewHolder) holder2;

            final Image user = users.get(position);
            ((Reddit) main.getApplicationContext()).getImageLoader().displayImage(user.getImageUrl(), holder.image, ImageGridAdapter.options);
            holder.body.setVisibility(View.VISIBLE);
            holder.text.setVisibility(View.VISIBLE);
            View imageView = holder.image;
            if (imageView.getWidth() == 0) {
                holder.image.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            } else {
                holder.image.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) getHeightFromAspectRatio(user.getHeight(), user.getWidth(), imageView.getWidth())));
            }

            {
                if (user.getTitle() != null) {
                    List<String> text = SubmissionParser.getBlocks(user.getTitle());
                    holder.text.setText(Html.fromHtml(text.get(0))); // TODO deadleg determine behaviour. Add overflow
                    if (holder.text.getText().toString().isEmpty()) {
                        holder.text.setVisibility(View.GONE);
                    }

                } else {
                    holder.text.setVisibility(View.GONE);

                }
            }
            {
                if (user.getDescription() != null) {
                    List<String> text = SubmissionParser.getBlocks(user.getDescription());
                    holder.body.setText(Html.fromHtml(text.get(0))); // TODO deadleg determine behaviour. Add overflow
                    if (holder.body.getText().toString().isEmpty()) {
                        holder.body.setVisibility(View.GONE);
                    }
                } else {
                    holder.body.setVisibility(View.GONE);

                }
            }

            View.OnClickListener onGifImageClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SettingValues.image && !user.isAnimated() || SettingValues.gif && user.isAnimated()) {
                        Intent myIntent = new Intent(main, MediaView.class);
                        myIntent.putExtra(MediaView.EXTRA_URL, user.getImageUrl());
                        main.startActivity(myIntent);
                    } else {
                        Reddit.defaultShare(user.getImageUrl(), main);
                    }
                }
            };


            if (user.isAnimated()) {
                holder.body.setVisibility(View.VISIBLE);
                holder.body.setSingleLine(false);
                holder.body.setText(holder.text.getText() + main.getString(R.string.submission_tap_gif).toUpperCase()); //got rid of the \n thing, because it didnt parse and it was already a new line so...
                holder.body.setOnClickListener(onGifImageClickListener);
            }

            holder.itemView.setOnClickListener(onGifImageClickListener);
        } else if (holder2 instanceof SpacerViewHolder) {
            holder2.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(holder2.itemView.getWidth(), paddingBottom ? height : main.findViewById(R.id.toolbar).getHeight()));
        }

    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size() + 1;
    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        final SpoilerRobotoTextView text;
        final SpoilerRobotoTextView body;
        final ImageView image;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            text = (SpoilerRobotoTextView) itemView.findViewById(R.id.imagetitle);
            body = (SpoilerRobotoTextView) itemView.findViewById(R.id.imageCaption);
            image = (ImageView) itemView.findViewById(R.id.image);


        }
    }

}
