package com.ssiot.donghai.data.model.view;

import com.ssiot.donghai.data.model.NodeModel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NodeView2Model extends NodeModel{
    public Timestamp _updatetime;//cs代码中是在NodeModel的Node_show中
    public String _isonline = "离线";
    
    public List<NodeData> _nodeData_list = new ArrayList<NodeData>();
    
    //jingbo 写成内部类有问题，static方法调用时有问题
//    public class NodeData{
//        public String _name;
//        public float _data;
//        public String _unit;
//        public float _proportion;
//        public String _compare;
//    }
    
    
    public boolean showAll = false;//jingbo仅仅是UI使用的
    public String _detailTime = "";//jingbo 仅仅在MoniDataFrag用到的时间字符串
}