package wellingtontrains;

//This program is copyright VUW.
//You are granted permission to use it to construct your answer to a SWEN502 assignment.
//You may not distribute it in any other way without permission.

//Code for SWEN502, Assignment W2

import java.util.*;

/**
* TrainLine
* Information about a Train Line.
* Note, we treat the outbound train line as a different from the inbound line.
* This means that the Johnsonville-Wellington line is a different train line from 
* the Wellington-Johnsonville line.
* Although they have the same stations, the stations will be in opposite orders.
*
* A TrainLine contains 
* - the name of the TrainLine (originating station - terminal station, eg Wellington-Melling)
* - The list of stations on the line
* - a list of TrainServices running on the line (eg the 10:00 am service from Upper-Hutt to Wellington)
*   (in order of time - services earlier in the list are always earlier times (at any station) than later services  )
*/

public class TrainLine{
 //Fields
 private String name;
 private List<Station> stations = new ArrayList<Station>();             // list of stations on the line
 private List<TrainService> trainServices = new ArrayList<TrainService>(); // set of TrainServices running on the line

 //Constructor
 public TrainLine(String name){
     this.name = name;
 }

 // Methods to add values to the TrainLine
 /**
  * Add a TrainService to the set of TrainServices for this line
  */
 public void addTrainService(TrainService train){
     trainServices.add(train);
 }

 /**
  * Add a Station to the list of Stations on this line
  */
 public void addStation(Station station){
     stations.add(station);
 }

 //Getters
 public String getName(){
     return name;
 }

 public List<Station> getStations(){
     return Collections.unmodifiableList(stations); // an unmodifiable version of the list of stations
 }

 public List<TrainService> getTrainServices(){
     return Collections.unmodifiableList(trainServices); // an unmodifiable version of the list of trainServices
 }

 /**
  * String contains name of the train line name plus number of stations and number of services
  */
 public String toString(){
     return (name+" ("+stations.size()+" stations, "+trainServices.size()+" services)");
 }

}

