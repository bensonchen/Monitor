package leavesc.hello.monitor.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.List;

import leavesc.hello.monitor.R;
import leavesc.hello.monitor.db.entity.MonitorHttpInformation;

/**
 * 作者：leavesC
 * 时间：2019/2/9 13:55
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class MonitorAdapter extends RecyclerView.Adapter<MonitorAdapter.MonitorViewHolder> {

    public interface OnClickListener {
        void onClick(int position, MonitorHttpInformation model);
    }

    private final int colorDefault;
    private final int colorRequested;
    private final int colorError;
    private final int color500;
    private final int color400;
    private final int color300;

    private OnClickListener clickListener;

    private AsyncListDiffer<MonitorHttpInformation> asyncListDiffer;

    public MonitorAdapter(Context context) {
        asyncListDiffer = new AsyncListDiffer<>(this, new DiffUtilItemCallback());
        colorDefault = ContextCompat.getColor(context, R.color.itemTitleColor);
        colorRequested = ContextCompat.getColor(context, R.color.monitor_status_requested);
        colorError = ContextCompat.getColor(context, R.color.monitor_status_error);
        color500 = ContextCompat.getColor(context, R.color.monitor_status_500);
        color400 = ContextCompat.getColor(context, R.color.monitor_status_400);
        color300 = ContextCompat.getColor(context, R.color.monitor_status_300);
    }

    public void setData(List<MonitorHttpInformation> dataList) {
        asyncListDiffer.submitList(dataList);
    }

    public void clear() {
        asyncListDiffer.submitList(null);
    }

    @NonNull
    @Override
    public MonitorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MonitorAdapter.MonitorViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull final MonitorViewHolder holder, int position) {
        final MonitorHttpInformation transaction = asyncListDiffer.getCurrentList().get(position);
        holder.tv_path.setText(String.format("%s %s", transaction.getMethod(), transaction.getPath()));
        holder.tv_host.setText(transaction.getHost());
        holder.tv_requestDate.setText(transaction.getRequestDateFormat());
        holder.iv_ssl.setVisibility(transaction.isSsl() ? View.VISIBLE : View.GONE);
        if (transaction.getStatus() == MonitorHttpInformation.Status.Complete) {
            holder.tv_code.setText(String.valueOf(transaction.getResponseCode()));
            holder.tv_duration.setText(MessageFormat.format("{0}ms", transaction.getDuration()));
            holder.tv_size.setText(transaction.getTotalSizeString());
        } else {
            holder.tv_code.setText(null);
            holder.tv_duration.setText(null);
            holder.tv_size.setText(null);
        }
        if (transaction.getStatus() == MonitorHttpInformation.Status.Failed) {
            holder.tv_code.setText("!!!");
        }
        setStatusColor(holder, transaction);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onClick(holder.getAdapterPosition(), transaction);
                }
            }
        });
    }

    private void setStatusColor(MonitorViewHolder holder, MonitorHttpInformation transaction) {
        int color;
        if (transaction.getStatus() == MonitorHttpInformation.Status.Failed) {
            color = colorError;
        } else if (transaction.getStatus() == MonitorHttpInformation.Status.Requested) {
            color = colorRequested;
        } else if (transaction.getResponseCode() >= 500) {
            color = color500;
        } else if (transaction.getResponseCode() >= 400) {
            color = color400;
        } else if (transaction.getResponseCode() >= 300) {
            color = color300;
        } else {
            color = colorDefault;
        }
        holder.tv_code.setTextColor(color);
        holder.tv_path.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return asyncListDiffer.getCurrentList().size();
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    static class MonitorViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final TextView tv_code;
        public final TextView tv_path;
        public final TextView tv_host;
        public final ImageView iv_ssl;
        public final TextView tv_requestDate;
        public final TextView tv_duration;
        public final TextView tv_size;

        MonitorViewHolder(@NonNull ViewGroup viewGroup) {
            super(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_monitor, viewGroup, false));
            this.view = itemView;
            tv_code = view.findViewById(R.id.tv_code);
            tv_path = view.findViewById(R.id.tv_path);
            tv_host = view.findViewById(R.id.tv_host);
            iv_ssl = view.findViewById(R.id.iv_ssl);
            tv_requestDate = view.findViewById(R.id.tv_requestDate);
            tv_duration = view.findViewById(R.id.tv_duration);
            tv_size = view.findViewById(R.id.tv_size);
        }

    }

}