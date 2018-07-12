package com.wen.asyl.movepinphotodemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //当前移动动画是否正在执行
    private boolean isAnimRun=false;
    //判断游戏是否开始
    private boolean isGameStart=false;
    private GridLayout mGridLayout;
    //利用二维数组创建若干个游戏小方块
    private ImageView[][] iv_game_arr=new ImageView[3][5];
     //当前空方块的实例
    private ImageView mIvNullImageView;
    //当前手势
    private GestureDetector mDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDetector=new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int ges = getDirByGes(e1.getX(), e1.getY(), e2.getX(), e2.getY());
                changeByDir(ges);
                return false;
            }
        });
        //初始化游戏的若干个小方块
        //获取一张大图
        Bitmap bigBm = BitmapFactory.decodeResource(getResources(), R.drawable.pic_1);
       // Bitmap bigBm = BitmapFactory.decodeResource(getResources(), R.drawable.a);
        // 小方块边长
        int tuWandH = bigBm.getWidth() / 5;
        // 小方块的边长，应该是整个屏幕的宽度/5
        @SuppressWarnings("deprecation")
        int ivWandH = getWindowManager().getDefaultDisplay().getWidth() / 5;
        for (int i=0;i<iv_game_arr.length;i++){
            for (int j=0;j<iv_game_arr[0].length;j++){
                // 根据行和列来切成若干个游戏小图片(根据列来切的)
                Bitmap bm = Bitmap.createBitmap(bigBm, j * tuWandH, i * tuWandH, tuWandH, tuWandH);
                iv_game_arr[i][j] = new ImageView(this);
                // 设置每一个游戏小方块的图案
                iv_game_arr[i][j].setImageBitmap(bm);
                // 他的父布局是RL
                iv_game_arr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(ivWandH, ivWandH));
                // 设置小方块之间的间距
                iv_game_arr[i][j].setPadding(2, 2, 2, 2);
                iv_game_arr[i][j].setTag(new GameData(i,j,bm));
                iv_game_arr[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = isNearByNullImageView((ImageView) v);
                        if (flag){
                            Log.e("flag",flag+"");
                            changeDataByImageView((ImageView) v);
                        }

                    }
                });

            }
        }
        //初始化游戏主界面，并添加若干个游戏小方块
        mGridLayout= (GridLayout) findViewById(R.id.gl_main_game);
        for (int i=0;i<iv_game_arr.length;i++){
            for (int j=0;j<iv_game_arr[0].length;j++){
                mGridLayout.addView(iv_game_arr[i][j]);
            }
        }
        //设置最后一个方块是空的
        setNullImageView(iv_game_arr[2][4]);
        //随机打乱顺序方块
        randomMove();
        isGameStart=true;//开始状态
    }
    /**
     * 根据手势的方向，获取与空方块相邻的位置，如果存在方块就进行数据交换
     * @param type 1:上 2:下 3:左 4：右
     *
     */
    public void changeByDir(int type) {
        changeByDir(type,true);
    }
    /**
     * 根据手势的方向，获取与空方块相邻的位置，如果存在方块就进行数据交换
     * @param type 1:上 2:下 3:左 4：右
     * @param isAnim true 有动画 false 没有动画
     */
    public void changeByDir(int type,boolean isAnim) {
        //获取当前空方块的位置
         GameData mNullGameData= (GameData) mIvNullImageView.getTag();
        //根据方向，设置相应的相邻的位置的坐标
        int new_x = mNullGameData.x;
        int new_y = mNullGameData.y;
        if(type==1){ //要移动的方块在当前空方块的下边
            new_x++;
        }else  if(type==2){
            new_x--;
        }else  if(type==3){
            new_y++;
        }else  if(type==4){
            new_y--;
        }
        //判断这个新坐标，是否存在
        if (new_x>=0&&new_x<iv_game_arr.length&&new_y>=0&&new_y<iv_game_arr[0].length){
              //存在的话，可以移动
            if (isAnim){
                changeDataByImageView(iv_game_arr[new_x][new_y]);
            }else{
                changeDataByImageView(iv_game_arr[new_x][new_y],isAnim);
            }
        }else{
            //什么也不操作
        }

        //存在的话，开始移动
    }

    /**
     * 判断游戏结束的方法
     */
    public void isGameOver(){
        boolean isGameOver=true;
        //遍历每个游戏小方块
        for (int i=0;i<iv_game_arr.length;i++){
            for (int j=0;j<iv_game_arr[0].length;j++){
                //为空的方块数据不判断跳过
                if (iv_game_arr[i][j]==mIvNullImageView){
                    continue;
                }
              GameData mGameData= (GameData) iv_game_arr[i][j].getTag();
                if (!mGameData.isTrue()){
                    isGameOver=false;
                    break;
                }
            }
        }
        //根据一个开关变量决定游戏是否结束，结束时给提示
        if(isGameOver){
            Toast.makeText(this, "游戏结束", Toast.LENGTH_SHORT).show();
        }

    }
    /**
     * 手势判断，是向左滑还是向右滑
     * @param start_x 手势的起始点x
     * @param start_y 手势的起始点y
     * @param end_x 手势的终止点x
     * @param end_y 手势的终止点y
     * @return 1:上 2:下 3:左 4：右
     */
    public  int getDirByGes(float start_x, float start_y, float end_x, float end_y){
        // 左右：横向距离大于竖直距离
        // 左 ：终点x小于起点x
        // 安卓y正轴方向为竖直向下
        // 上：终点y小于起点y
        boolean isLeftOrRight=(Math.abs(start_x-end_x)>Math.abs(start_y-end_y)?true:false);//是否是左右
           if (isLeftOrRight){ //左右
               boolean isLeft=start_x-end_x>0?true:false;
               if (isLeft){
                   return 3;
               }else{
                   return 4;
               }
           }else {//上下
               boolean isUp=start_y-end_y>0?true:false;
               if (isUp){
                   return 1;
               }else{
                   return 2;
               }
           }
    }

    /**
     * 随机打乱顺序
     */
    public void randomMove(){
        //打乱的次数
        for (int i=0;i<150;i++){
            //根据手势开始交换，无动画
            int type= (int) ((Math.random()*4)+1);
            changeByDir(type,false);
        }

    }
    /**
     * 利用动画结束之后，交换两个方块的数据
     * @param imageView
     */
    public  void changeDataByImageView(final ImageView imageView){
        changeDataByImageView(imageView,true);
    }
    /**
     * 利用动画结束之后，交换两个方块的数据
     * @param imageView
     * @param isAnim true 有动画，false 没动画
     */
    public  void changeDataByImageView(final ImageView imageView, final boolean isAnim){
         if (isAnimRun){
             return;
         }
        if (!isAnim){
             GameData mGameData= (GameData) imageView.getTag();
             mIvNullImageView.setImageBitmap(mGameData.bm);
             GameData mNullGameData= (GameData) mIvNullImageView.getTag();
             mNullGameData.bm=mGameData.bm;
             mNullGameData.p_x=mGameData.p_x;
             mNullGameData.p_y=mGameData.p_y;
             //设置当前点击的为空方块
             setNullImageView(imageView);
             if (!isGameStart){
                 isGameOver();
             }
             return;
         }
        //创建一个动画，设置好方向，移动的距离
        TranslateAnimation translateAnimation=null;
        if (imageView.getX()>mIvNullImageView.getX()){//当前点击的方块在空方块的下边
           //往上移动
            translateAnimation=new TranslateAnimation(0.1f,-imageView.getWidth(),0.1f,0.1f);
        }else if (imageView.getX()<mIvNullImageView.getX()){
            //往下移动
            translateAnimation=new TranslateAnimation(0.1f,imageView.getWidth(),0.1f,0.1f);
        }else if (imageView.getY()>mIvNullImageView.getY()){
            //往左移动
            translateAnimation=new TranslateAnimation(0.1f,0.1f,0.1f,-imageView.getWidth());
        }else if (imageView.getY()<mIvNullImageView.getY()){
            //往右移动
            translateAnimation=new TranslateAnimation(0.1f,0.1f,0.1f,imageView.getWidth());
        }
        //设置动画的时长
        translateAnimation.setDuration(70);
        //设置动画结束之后是否停留
        translateAnimation.setFillAfter(true);
        //设置动画结束之后要真正的把数据交换了
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimRun=true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimRun=false;
              imageView.clearAnimation();
              GameData mGameData= (GameData) imageView.getTag();
                mIvNullImageView.setImageBitmap(mGameData.bm);
                GameData mNullGameData= (GameData) mIvNullImageView.getTag();
                mNullGameData.bm=mGameData.bm;
                mNullGameData.p_x=mGameData.p_x;
                mNullGameData.p_y=mGameData.p_y;
                //设置当前点击的为空方块
                setNullImageView(imageView);
                if (!isGameStart){
                    isGameOver();
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //执行动画
        imageView.startAnimation(translateAnimation);

    }
    /**
     * 设置某个方块为空方块
     * @param imageView
     */
    public  void setNullImageView(ImageView imageView){
        imageView.setImageBitmap(null);
        mIvNullImageView=imageView;
    }

    /**
     * 判断当前点击的方块，是否与空方块的位置关系是相邻关系
     * @param imageView
     * @return true 相邻 false 不相邻
     */
    public boolean isNearByNullImageView(ImageView imageView){
        //分别获取当前空方块的位置与点击方块的位置，通过坐标x、y两边只差1的方式判断
        GameData mNullGameData = (GameData) mIvNullImageView.getTag();
        GameData mGameData = (GameData) imageView.getTag();
        if (mNullGameData.y==mGameData.y&&mGameData.x+1==mNullGameData.x){
            //当前点击的方块在空方块的上方
             return  true;
        }else if (mNullGameData.y==mGameData.y&&mGameData.x-1==mNullGameData.x){
            //当前点击的方块在空方块的下方
            return  true;
        }else if (mNullGameData.y==mGameData.y+1&&mGameData.x==mNullGameData.x){
            //当前点击的方块在空方块的左边
            return  true;
        }else if (mNullGameData.y==mGameData.y-1&&mGameData.x==mNullGameData.x){
            //当前点击的方块在空方块的右边
            return  true;
        }
        return  false;
    }

    /**
     * 每个游戏小方块上要绑定的数据
     */
    class GameData{
        //每个小方块的实际位置x
        public int x=0;
        //每个小方块的实际位置y
        public int y=0;
        //每个小方块的图片
        public  Bitmap bm;
        //每个小方块的图片的位置
        public  int p_x=0;
        public  int p_y=0;

        public GameData(int x, int y, Bitmap bm) {
            this.x = x;
            this.y = y;
            this.bm = bm;
            this.p_x = x;
            this.p_y = y;
        }

        /**
         * 每个小方块的位置是否正确
         * @return true 正确 false 不正确
         */
        public boolean isTrue() {
            if (x==p_x&&y==p_y){
                return true;
            }
                return  false;
        }
    }

}
