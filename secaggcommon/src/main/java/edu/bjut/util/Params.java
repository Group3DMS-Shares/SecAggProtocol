package edu.bjut.util;

import java.math.BigInteger;

public class Params {
    
//  public final static int EXPERIMENT_REPEART_TIMES = 500; /* times of running the experiments */
//  public final static int EXPERIMENT_REPEART_TIMES = 2; /* times of running the experiments */
    public final static int EXPERIMENT_REPEART_TIMES = 1; /* times of running the experiments */
    
    
//  public static int ARRAY_OF_PARTICIPANT_NUM[] = {10,20,30,40,50,60}; /* number of smart meters */
//  public static int METERS_NUM = 20; /* number of smart meters */
//  public static int ARRAY_OF_PARTICIPANT_NUM[] = {5,6,7,8,9,10}; /* number of smart meters */
    public static int ARRAY_OF_PARTICIPANT_NUM[] = {10}; /* number of smart meters */
    public static int PARTICIPANT_NUM = 5; /* number of smart meters */
//  public static int ARRAY_OF_PARTICIPANT_FAILS[] = {1,2,3,4,5}; /* number of smart meters */
    public static int ARRAY_OF_PARTICIPANT_FAILS[] = {10}; /* number of smart meters */
    public static int ARRAY_OF_PARTICIPANT_FAILSII[] = {6}; /* number of smart meters */
    public static int fails[];
    public static int PARTICIPANT_FAILS = 1; /* number of smart meters */
    public static int ARRAY_OF_RECOVER_K[] = {1,2,3,4,5}; /* number of smart meters */
    public static int RECOVER_K = 2; /* number of smart meters */
    
    public static int UPBOUND_LIMIT_OF_METER_DATA = 1000; /* upper bound of a meter's reporting data */
    
    public static BigInteger smallMod = new BigInteger("1152921504606846976");
}
