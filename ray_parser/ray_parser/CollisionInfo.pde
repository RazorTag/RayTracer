class CollisionInfo {
  public float t;
  public Point location;
  public Normal normal;
  public Surface surface;
  
  CollisionInfo(float t, Point location, Normal normal, Surface surface) {
    this.t = t;
    this.location = location;
    this.normal = normal;
    this.surface = surface;
  }
  
  CollisionInfo() {
    t = -1.0;
    location = null;
    normal = null;
    surface = null;
  }
  
  CollisionInfo clone() {
    return(new CollisionInfo(t, location.clone(), normal.clone(), surface.clone()));
  }
}
