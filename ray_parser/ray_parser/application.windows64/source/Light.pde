class Light {
  public Point location;
  public Color rgb;
  
  Light (float x, float y, float z, float r, float g, float b) {
    location = new Point(x, y, z);
    rgb = new Color(r, g, b);
  }
  
  //Does the ray hit this light?
  boolean collision (Ray ray, ArrayList<Shape> shapes) {
    float closest = ray.nearestCollision(shapes).t;
    float distance = location.distance(ray.origin);
    if(closest < 0.0 || closest > distance) {return true;}
    else {return false;}
  }
  
  float x() {
    return location.x(); 
  }
  
  float y() {
    return location.y(); 
  }
  
  float z() {
    return location.z(); 
  }
  
  Color getColor() {
    return rgb;
  }
}
