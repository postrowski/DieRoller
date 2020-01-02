package com.ostrowski.dieroller;

public enum RollState {
   DROPPING,
   BOUNCE_1,
   BOUNCE_2,
   BOUNCE_3,
   BOUNCE_4,
   LEVELING,
   STOPPED;

   public RollState getNextState() {
      //System.out.println("going to next state from " + this.name());
      switch (this) {
         case DROPPING: return BOUNCE_1;
         case BOUNCE_1: return BOUNCE_2;
         case BOUNCE_2: return BOUNCE_3;
         case BOUNCE_3: return BOUNCE_4;
         case BOUNCE_4: return LEVELING;
         case LEVELING: return STOPPED;
         case STOPPED: return STOPPED;
      }
      return STOPPED;
   }
};
