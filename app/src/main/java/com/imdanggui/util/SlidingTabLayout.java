package com.imdanggui.util;

/**
 * Created by user on 2015-09-30.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imdanggui.R;

public class SlidingTabLayout extends HorizontalScrollView {

    public interface TabColorizer {

        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

    }

    private static final int TITLE_OFFSET_DIPS = 44;
    private static final int TAB_VIEW_PADDING_DIPS = 10;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;
    int density;
    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;
    private boolean mDistributeEvenly;

    private ViewPager mViewPager;
    private SparseArray<String> mContentDescriptions = new SparseArray<String>();
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    private final SlidingTabStrip mTabStrip;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);
        // Make sure that the Tab Strips fills this View
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        mTabStrip = new SlidingTabStrip(context);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    public void setDistributeEvenly(boolean distributeEvenly) {
        mDistributeEvenly = distributeEvenly;
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId id of the {@link android.widget.TextView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId , int density) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
        this.density = density;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        mTabStrip.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }


/*    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                outValue, true);
        textView.setBackgroundResource(outValue.resourceId);
        textView.setAllCaps(true);

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);

        return textView;
    }*/
    protected ImageView createDefaultTabView(Context context) {
        ImageView imageView = new ImageView(context);

        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                outValue, true);
        imageView.setBackgroundResource(R.drawable.tab_01_y);



        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        imageView.setPadding(padding, padding, padding, padding);

        return imageView;
    }

    private void populateTabStrip() {
        final PagerAdapter adapter = mViewPager.getAdapter();
        final OnClickListener tabClickListener = new TabClickListener();
        mTabStrip.setWeightSum(3);

        for (int i = 0; i < adapter.getCount(); i++) {
            View tabView = null;
            TextView tabTitleView = null;


            if (mTabViewLayoutId != 0) {
                Dlog.d("#######  layout != 0");
                // If there is a custom tab view layout id set, try and inflate it
                /*tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip,
                        false);*/
                if(i == 0){
                    tabView = LayoutInflater.from(getContext()).inflate(R.layout.item_tab1_unselected, mTabStrip,
                            false);
                }else if( i == 1){
                    tabView = LayoutInflater.from(getContext()).inflate(R.layout.item_tab2, mTabStrip,
                            false);
                }else {
                    tabView = LayoutInflater.from(getContext()).inflate(R.layout.item_tab3_unselected, mTabStrip,
                            false);
                }
                int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
                tabView.setPadding(padding, padding, padding, padding);
                //tabView.setMinimumWidth(density);


                //tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
            }

            if (tabView == null) {
                Dlog.d("####### tabview == null");

                tabView = createDefaultTabView(getContext());
                if( density != 0){
                    tabView.setMinimumWidth(density);
                }
                if(i == 0){
                    tabView.setBackgroundResource(R.drawable.tab_02_y);
                }else if( i == 1){
                    tabView.setBackgroundResource(R.drawable.tab_01_y);
                }else {
                    tabView.setBackgroundResource(R.drawable.tab_03_y);
                }
            }

            if (tabTitleView == null && TextView.class.isInstance(tabView)) {
                tabTitleView = (TextView) tabView;
            }
            LinearLayout.LayoutParams lap = (LinearLayout.LayoutParams) tabView.getLayoutParams();
            lap.width = 0;
            lap.weight = 1;

            if (mDistributeEvenly) {
                Dlog.d("#########evenly ###########");
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                lp.width = 0;
                lp.weight = 1;
            }

            //tabTitleView.setText(adapter.getPageTitle(i));
            //tabTitleView.setTextColor(Color.WHITE);
            tabView.setOnClickListener(tabClickListener);
            String desc = mContentDescriptions.get(i, null);
            if (desc != null) {
                tabView.setContentDescription(desc);
            }

            mTabStrip.addView(tabView);
            if (i == mViewPager.getCurrentItem()) {
                tabView.setSelected(true);
            }
        }
    }

    public void setContentDescription(int i, String desc) {
        mContentDescriptions.put(i, desc);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (tabIndex > 0 || positionOffset > 0) {
                // If we're not at the first child and are mid-scroll, make sure we obey the offset
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;
        ImageView tab1;
        //ImageView tab2;
        //ImageView tab3;
        TextView tab1_tv;
        TextView tab2_tv;
        TextView tab3_tv;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Dlog.d("###position### : " + String.valueOf(position));
            Dlog.d("###position off set### : " + String.valueOf(positionOffset));
            tab1 = (ImageView) mTabStrip.getChildAt(0).findViewById(R.id.tab1);
            //tab2 = (ImageView) mTabStrip.getChildAt(1).findViewById(R.id.tab2);
            //tab3 = (ImageView) mTabStrip.getChildAt(2).findViewById(R.id.tab3);
            tab1_tv = (TextView) mTabStrip.getChildAt(0).findViewById(R.id.tab1_tv);
            tab2_tv = (TextView) mTabStrip.getChildAt(1).findViewById(R.id.tab2_tv);
            tab3_tv = (TextView) mTabStrip.getChildAt(2).findViewById(R.id.tab3_tv);

            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(position);
            if(position == 0){
                tab1.setBackground(getResources().getDrawable(R.drawable.tab_02_y));
                //tab2.setBackground(getResources().getDrawable(R.drawable.tab_01_w));
                //tab3.setBackground(getResources().getDrawable(R.drawable.tab_03_w));
                tab1_tv.setTextColor(getResources().getColor(R.color.selected_tab_icon));
                tab2_tv.setTextColor(getResources().getColor(R.color.unselect_tab_icon));
                tab3_tv.setTextColor(getResources().getColor(R.color.unselect_tab_icon));
            }else if(position == 1){
                tab1.setBackground(getResources().getDrawable(R.drawable.tab_02_w));
                //tab2.setBackground(getResources().getDrawable(R.drawable.tab_01_y));
                //tab3.setBackground(getResources().getDrawable(R.drawable.tab_03_w));
                tab1_tv.setTextColor(getResources().getColor(R.color.unselect_tab_icon));
                tab2_tv.setTextColor(getResources().getColor(R.color.selected_tab_icon));
                tab3_tv.setTextColor(getResources().getColor(R.color.unselect_tab_icon));
            }else if(position == 2){
                tab1.setBackground(getResources().getDrawable(R.drawable.tab_02_w));
                //tab2.setBackground(getResources().getDrawable(R.drawable.tab_01_w));
                //tab3.setBackground(getResources().getDrawable(R.drawable.tab_03_y));
                tab1_tv.setTextColor(getResources().getColor(R.color.unselect_tab_icon));
                tab2_tv.setTextColor(getResources().getColor(R.color.unselect_tab_icon));
                tab3_tv.setTextColor(getResources().getColor(R.color.selected_tab_icon));
            }

            //ImageView tab1 = (ImageView)selectedTitle.findViewById(R.id.tab1);
            //tab1.setBackground(getResources().getDrawable(R.drawable.tab_02_y));
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            Dlog.d("######pageSelected : position : " + String.valueOf(position));
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                mTabStrip.getChildAt(i).setSelected(position == i);
            }
            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }

    private class TabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }

}