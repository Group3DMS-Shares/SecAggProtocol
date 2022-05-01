package edu.bjut.aaia.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unisa.dia.gas.jpbc.Element;

public class MsgResponseRound0 {
    private List<MsgRound0> pubKeys;
    private Map<Integer, Element> cPk_uMap;

    public MsgResponseRound0(List<MsgRound0> pubKeys) {
        this.pubKeys = pubKeys;
    }

    public List<MsgRound0> getPubKeys() {
        return pubKeys;
    }

    public Map<Integer, Element> getcPk_uMap() {
        return cPk_uMap;
    }

    public void setcPk_uMap(Map<Integer, Element> cPk_uMap) {
        this.cPk_uMap = cPk_uMap;
    }

    public void setPubKeys(List<MsgRound0> pubKeys) {
        this.pubKeys = pubKeys;
    }

    public MsgResponseRound0(List<MsgRound0> pubKeys, Map<Integer, Element> cPk_uMap) {
        this.pubKeys = pubKeys;
        this.setcPk_uMap(cPk_uMap);
    }
}
