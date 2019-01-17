package group.tonight.roommigrationdemo;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import group.tonight.firimsdk.SampleUpdateDialog;
import group.tonight.roommigrationdemo.databinding.ActivityMainBinding;
import group.tonight.roommigrationdemo.model.Cat;
import group.tonight.roommigrationdemo.model.Dog;
import group.tonight.roommigrationdemo.model.Word;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    private List<Word> mDataList;
    private ItemAdapter mItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewDataBinding dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBinding.setVariable(BR.handler, this);

        ActivityMainBinding activityMainBinding = (ActivityMainBinding) dataBinding;
        mRecyclerView = activityMainBinding.recyclerView;
        mDataList = new ArrayList<>();
        mItemAdapter = new ItemAdapter(this, mDataList);
        mRecyclerView.setAdapter(mItemAdapter);

        AppDatabase.getDatabase(MainActivity.this).wordDao().loadAll().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(@Nullable List<Word> words) {
                if (words != null) {
                    mDataList.clear();
                    mDataList.addAll(words);
                    mItemAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Word word = new Word();
                        word.setWord("word" + Math.random());
                        AppDatabase.getDatabase(MainActivity.this).wordDao().insert(word);

                        Dog dog = new Dog();
                        dog.setName("dog:" + ((int) (Math.random() * 10000)));
                        AppDatabase.getDatabase(MainActivity.this).dogDao().insert(dog);

                        Cat cat = new Cat();
                        cat.setName("cat:" + ((int) (Math.random() * 10000)));
                        AppDatabase.getDatabase(MainActivity.this).catDao().insert(cat);
                    }
                }).start();
                break;
            case R.id.update:
                Toast.makeText(this, "修改", Toast.LENGTH_SHORT).show();
                break;
            case R.id.check_update:
                SampleUpdateDialog.getInstance("pud6").show(getSupportFragmentManager(), SampleUpdateDialog.class.getSimpleName());
                break;
            default:
                break;
        }
    }


    private static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
        private Context context;
        private List<Word> dataList;

        public ItemAdapter(Context context, List<Word> dataList) {
            this.context = context;
            this.dataList = dataList;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return ItemViewHolder.create(LayoutInflater.from(context), R.layout.simple_list_item_1, viewGroup, false);
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
            itemViewHolder.getDataBinding().setVariable(BR.data, dataList.get(i));
            itemViewHolder.getDataBinding().setVariable(BR.handler, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = itemViewHolder.getAdapterPosition();
                    new AlertDialog.Builder(v.getContext())
                            .setMessage("删除？")
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AppDatabase.getDatabase(((AlertDialog) dialog).getContext()).wordDao().delete(dataList.get(position));
                                        }
                                    }).start();
                                }
                            })
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        private static class ItemViewHolder extends RecyclerView.ViewHolder {
            static ItemViewHolder create(@NonNull LayoutInflater inflater, int layoutId, @Nullable ViewGroup parent, boolean attachToParent) {
                ViewDataBinding dataBinding = DataBindingUtil.inflate(inflater, layoutId, parent, attachToParent);
                return new ItemViewHolder(dataBinding);
            }

            private ViewDataBinding dataBinding;

            private ItemViewHolder(ViewDataBinding dataBinding) {
                super(dataBinding.getRoot());
                this.dataBinding = dataBinding;
            }

            private ViewDataBinding getDataBinding() {
                return dataBinding;
            }
        }
    }
}
