package org.jarvis.code.core.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jarvis.code.R;
import org.jarvis.code.core.component.GalleryDialog;
import org.jarvis.code.core.fragment.RegisterFragment;
import org.jarvis.code.core.model.response.Product;
import org.jarvis.code.util.Constant;

import java.util.List;

/**
 * Created by KimChheng on 6/2/2017.
 */

public class WeddingAdapter extends RecyclerView.Adapter {

    private final int VIEW_ITEM = 0;
    private final int VIEW_PROMOTE = 1;
    private final int VIEW_LOADING = 2;

    private List<Product> data;
    private Context context;

    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;

    private OnLoadMoreListener onLoadMoreListener;
    private static String imgUrl = Constant.BASE_URL + "mobile/image/view/";

    public WeddingAdapter(Context context, List<Product> products) {
        this.data = products;
        this.context = context;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();
            recyclerView
                    .addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            totalItemCount = linearLayoutManager.getItemCount();
                            lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                if (onLoadMoreListener != null) {
                                    onLoadMoreListener.onLoadMore();
                                }
                                loading = true;
                            }
                        }
                    });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wedding, parent, false);
            return new ProductViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progressbar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProductViewHolder) {
            Product product = (Product) data.get(position);
            ProductViewHolder productViewHolder = (ProductViewHolder) holder;
            productViewHolder.product = product;
            productViewHolder.lblCode.setText(context.getResources().getString(R.string.string_code) + ":" + product.getCode());
            productViewHolder.lblColor.setText(context.getResources().getString(R.string.string_color) + ":" + product.getColor());
            productViewHolder.lblSize.setText(context.getResources().getString(R.string.string_size) + ":" + product.getSize());
            productViewHolder.lblPrice.setText(context.getResources().getString(R.string.string_price) + ":" + product.getPrice());
            Picasso.with(context).load(imgUrl + product.getImages().get(0).getId())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.no_image_available)
                    .into(productViewHolder.image);
        } else {
            ((LoadingViewHolder) holder).progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position) != null ? VIEW_ITEM : VIEW_LOADING;
    }

    public void addAll(List<Product> products) {
        data.addAll(products);
        notifyDataSetChanged();
    }

    public void add(Product product) {
        data.add(product);
        notifyItemInserted(data.size());
    }

    public void setLoaded() {
        loading = false;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView lblCode;
        public TextView lblPrice;
        public TextView lblSize;
        public TextView lblColor;
        public TextView lblRegister;
        public ImageView image;
        public Product product;

        public ProductViewHolder(View itemView) {
            super(itemView);
            lblCode = (TextView) itemView.findViewById(R.id.lblCode);
            lblPrice = (TextView) itemView.findViewById(R.id.lblPrice);
            lblSize = (TextView) itemView.findViewById(R.id.lblSize);
            lblColor = (TextView) itemView.findViewById(R.id.lblColor);
            lblRegister = (TextView) itemView.findViewById(R.id.lblRegister);
            lblRegister.setPaintFlags(lblRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            image = (ImageView) itemView.findViewById(R.id.imgViewProduct);
            image.setOnClickListener(this);
            lblRegister.setOnClickListener(this);
            lblRegister.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
            switch (view.getId()) {
                case R.id.lblRegister:
                    Bundle bundles = new Bundle();
                    bundles.putSerializable("product", product);
                    RegisterFragment registerFragment = new RegisterFragment();
                    registerFragment.setArguments(bundles);
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, registerFragment)
                            .addToBackStack("registerFragment")
                            .commit();
                    break;
                case R.id.imgViewProduct:
                    GalleryDialog galleryDialog = new GalleryDialog();
                    galleryDialog.setProduct(product);
                    galleryDialog.show(fragmentManager.beginTransaction(), "Image Gallery");
                    //galleryDialog.setCancelable(false);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            Toast.makeText(view.getContext(), "position = " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

}
