import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ray_parser extends PApplet {

///////////////////////////////////////////////////////////////////////
//
// Command Line Interface (CLI) Parser  
//
///////////////////////////////////////////////////////////////////////


String gCurrentFile = new String("rect_test.cli"); // A global variable for holding current active file name.
final float aspect = 1.0f;
float fov;
float centerX;
float centerY;
Color background;
int pixelColor;
Surface surface;
ArrayList<Light> lights = new ArrayList<Light>();
ArrayList<Shape> shapes = new ArrayList<Shape>();
ArrayList<Point> vertices = new ArrayList<Point>();
ArrayList<Pair> toBeDrawn = new ArrayList<Pair>();
Random rand = new Random();
Point viewer;
Point origin;
Normal direction;
int renderType;
final int renderTypes = 2;

final int timeout = 10000000;
public boolean rendering;
public final int divisions = 10;
public int slideDirection;
public int xDivision;
public int yDivision;
public int counter;
public int triangleCollision;
public int sphereCollision;
public String saveFile;


///////////////////////////////////////////////////////////////////////
//
// Press key 1 to 9 and 0 to run different test cases.
//
///////////////////////////////////////////////////////////////////////
public void keyPressed() {
  if(!rendering) {
    switch(key) {
      case '1':  gCurrentFile = new String("t0.cli"); interpreter(); break;
      case '2':  gCurrentFile = new String("t1.cli"); interpreter(); break;
      case '3':  gCurrentFile = new String("t2.cli"); interpreter(); break;
      case '4':  gCurrentFile = new String("t3.cli"); interpreter(); break;
      case '5':  gCurrentFile = new String("c0.cli"); interpreter(); break;
      case '6':  gCurrentFile = new String("c1.cli"); interpreter(); break;
      case '7':  gCurrentFile = new String("c2.cli"); interpreter(); break;
      case '8':  gCurrentFile = new String("c3.cli"); interpreter(); break;
      case '9':  gCurrentFile = new String("c4.cli"); interpreter(); break;
      case '0':  gCurrentFile = new String("c5.cli"); interpreter(); break;
    }
  }
}

///////////////////////////////////////////////////////////////////////
//
//  Parser core. It parses the CLI file and processes it based on each 
//  token. Only "color", "rect", and "write" tokens are implemented. 
//  You should start from here and add more functionalities for your
//  ray tracer.
//
//  Note: Function "splitToken()" is only available in processing 1.25 
//       or higher.
//
///////////////////////////////////////////////////////////////////////
public void interpreter() {
  loadDefaults();
  
  String str[] = loadStrings(gCurrentFile);
  if (str == null) println("Error! Failed to read the file.");
  for (int i=0; i<str.length; i++) {
    
    String[] token = splitTokens(str[i], " "); // Get a line and parse tokens.
    if (token.length == 0) continue; // Skip blank line.
    
    if (token[0].equals("fov")) {
      fov = parseValue(token[1]) * (PI / 180.0f);
    }
    else if (token[0].equals("background")) {
      background = new Color(parseValue(token[1]), parseValue(token[2]), parseValue(token[3]));
    }
    else if (token[0].equals("light")) {
      lights.add(new Light(parseValue(token[1]), parseValue(token[2]), parseValue(token[3]),
        parseValue(token[4]), parseValue(token[5]), parseValue(token[6])));
    }
    else if (token[0].equals("surface")) {
      surface = new Surface(parseValue(token[1]), parseValue(token[2]), 
        parseValue(token[3]), parseValue(token[4]), parseValue(token[5]), 
        parseValue(token[6]), parseValue(token[7]), parseValue(token[8]), 
        parseValue(token[9]), parseValue(token[10]), parseValue(token[11]));
    }    
    else if (token[0].equals("sphere")) {
      Point center = new Point(parseValue(token[2]), 
        parseValue(token[3]), parseValue(token[4]));
      shapes.add(new Sphere(surface, parseValue(token[1]), center));
    }
    else if (token[0].equals("begin")) {
      vertices.clear();
    }
    else if (token[0].equals("vertex")) {
      vertices.add(new Point(parseValue(token[1]), parseValue(token[2]), parseValue(token[3])));
    }
    else if (token[0].equals("end")) {
      if(vertices.size() == 3) {
        shapes.add(new Triangle(surface, vertices));
      }
      else if(vertices.size() < 3) {
        println("Less than 3 vertices");
      }
      else {
        println("Too many vertices");
      }
    }
    else if (token[0].equals("color")) {
      float r =PApplet.parseFloat(token[1]);
      float g =PApplet.parseFloat(token[2]);
      float b =PApplet.parseFloat(token[3]);
      fill(r, g, b);
    }
    else if (token[0].equals("rect")) {
      float x0 = PApplet.parseFloat(token[1])*(height/300);
      float y0 = PApplet.parseFloat(token[2])*(height/300);
      float x1 = PApplet.parseFloat(token[3])*(height/300);
      float y1 = PApplet.parseFloat(token[4])*(height/300);
      rect(x0, height-y1, x1-x0, y1-y0);
    }
    
    //This is where the rendering occurs
    else if (token[0].equals("write") && !gCurrentFile.equals("rect_test.cli")) {
      saveFile = token[1];
      rendering = true;
    }
  }
}

public Float parseValue(String s) {
  Float value;
  try {
    value = Float.parseFloat(s);
  }
  catch(Exception e) {
    value = (float) Integer.parseInt(s);
  }
  return value;
}

public void setPixelColor(int i, int j) {
  origin = new Point(viewer.x(), viewer.y(), viewer.z());
  direction = generateNormal(i, j);
  Ray ray = new Ray(origin, direction);
  Color shade = traceRay(ray, 0); //determine the color viewed at this pixel
  pixelColor = color(shade.getRed(), shade.getGreen(), shade.getBlue());
}

//returns the color seen along the specified ray
public Color traceRay(Ray ray, int iteration) {
  if(counter > timeout) {return background;}
  counter++;
  Ray reflectedRay;
  CollisionInfo info = ray.nearestCollision(shapes);
  Color shade; //the color of outgoing light (along path toward viewer away from collision)
  Color reflected; //the color of incoming light (along path toward viewer toward collision)
  Color spectral = new Color(0.0f, 0.0f, 0.0f); //specular highlight component
  Color diffuse = new Color(0.0f, 0.0f, 0.0f); //normalized sum of all incoming light (along any path that hits the collision point)
  Point originSlide;

  //find reflected color
  if(info.t < 0.0f || iteration > 50) { //reflection hits nothing or too many reflections have occurred
    return background;
  }
  else { //reflection hits another object so we find the incoming light on the reflection path
      originSlide = info.normal.scaleNormal(0.0001f).addPoints(info.location); //start slightly off of the surface so that new ray does not intersect the current surface
      reflectedRay = info.normal.reflect(ray, originSlide); //reflects the traced ray across the collision normal
      reflected = traceRay(reflectedRay, iteration+1); //recurse to find the incoming color for reflection
  }

  //find spectral and diffuse
  for(Light light : lights) {
    Normal V = ray.flip().direction; //ray from surface to eye
    Ray L = new Ray(originSlide, light.location.subtractPoints(originSlide)); //ray from surface to light source
    Normal R = info.normal.reflect(L.flip()); //reflection of ray from light source off of object roghly toward eye
    
    if(light.collision(L, shapes)) {
      float Kspec = pow(max(0.0f, V.dotProduct(R)), info.surface.P); //(V*R)^P

      spectral.addColor(light.rgb.scaleColor(Kspec)); //multiply color by Kspec
    
      //find diffuse component
      diffuse.addColor(light.rgb.scaleColor(max(0.0f, info.normal.dotProduct(L.unit())))); //Cl*max(0, N*L)
    }
  }
  
  //calculate the shade color
  shade = new Color( //ambient + diffuse + specular + reflected
    info.surface.Car() + diffuse.getRed()*info.surface.Cdr() + spectral.getRed()*info.surface.Csr() + reflected.getRed()*info.surface.Krefl(),
    info.surface.Cag() + diffuse.getGreen()*info.surface.Cdg() + spectral.getGreen()*info.surface.Csg() + reflected.getGreen()*info.surface.Krefl(),
    info.surface.Cab() + diffuse.getBlue()*info.surface.Cdb() + spectral.getBlue()*info.surface.Csb() + reflected.getBlue()*info.surface.Krefl()
  );
  return shade;
}

public Normal generateNormal(int i, int j) {
  float t = tan(fov/2);
  float z = 1;
  float xPan = t * ((i - centerX) / (height/2));
  float yPan = t * ((centerY - j) / (height/2));
  float zPan = -1;
  return (new Normal(xPan, yPan, zPan));
}

public float pythagorean(float x, float y) {
  return (sqrt(sq(x) + sq(y)));
}

public float pythagorean(float x, float y, float z) {
  return (sqrt(sq(x) + sq(y) + sq(z)));
}

public void loadDefaults() {
  rendering = false;
  counter = 0;
  triangleCollision = 0;
  sphereCollision = 0;
  fov = 60.0f;
  background = new Color(0.0f, 0.0f, 0.0f);
  lights.clear();
  shapes.clear();
  surface = new Surface(0.8f, 0.8f, 0.8f, 0.2f, 0.2f, 0.2f, 0.0f, 0.0f, 0.0f, 2.0f, 0.5f);
  viewer = new Point(0.0f, 0.0f, 0.0f);
  origin = new Point(0.0f, 0.0f, 0.0f);
  direction = new Normal(0.0f, 0.0f, -1.0f);
  pixelColor = color(0, 0, 0);
  xDivision = 0;
  yDivision = 0;
  slideDirection = 1;
  toBeDrawn.clear();
  loadDraw();
  renderType = rand.nextInt(renderTypes);
}

public void printSettings() {
  println("fov: " + fov);
  println("background: " + background.getRed() + "\t" + background.getGreen() + "\t" + background.getBlue());
  println("#lights: " + lights.size());
  println("#shapes: " + shapes.size());
  println("surface: " + surface.Cdr() + " " + surface.Cdg() + " " + surface.Cdb() + "\t\t"
    + surface.Car() + " " + surface.Cag() + " " + surface.Cab() + "\t"
    + surface.Csr() + " " + surface.Csg() + " " + surface.Csb() + "\t"
    + surface.P() + " " + surface.Krefl());
}

///////////////////////////////////////////////////////////////////////
//
// Some initializations for the scene.
//
///////////////////////////////////////////////////////////////////////
public void setup() {
  size(1000, 800);
  centerX = (width - 1) / 2;
  centerY = (height - 1) / 2;
  noStroke();
  colorMode(RGB, 1.0f);
  background(0, 0, 0);
  interpreter();
}

///////////////////////////////////////////////////////////////////////
//
// Draw frames.  Should leave this empty.
//
///////////////////////////////////////////////////////////////////////
public void draw() {
  if(gCurrentFile.equals("rect_test.cli")) {
    String s = "Each number key displays a unique rendering";
    int green = color(0, 255, 0);
    fill(green);
    textSize(30);
    text(s, 10, 10, width, 36);
  }
  if(rendering == true) {
    chooseRender();
  }
}

public void chooseRender() {
  switch(renderType) {
    case 0: slideRender(); break;
    case 1: randomRender(); break;
  }
}

public void slideRender() {
  int startX = (width/divisions)*xDivision;
  int startY = (height/divisions)*yDivision;
  render(startX, startY);
  xDivision += slideDirection;
  if(xDivision >= divisions) {
    slideDirection *= -1;
    xDivision += slideDirection;
    yDivision++;
  }
  if(xDivision < 0) {
    slideDirection *= -1;
    xDivision += slideDirection;
    yDivision++;
  }
  if(yDivision >= divisions) {
    rendering = false;
    save(saveFile);  
  }
}

public void randomRender() {
  int next = rand.nextInt(toBeDrawn.size());
  Pair draw = toBeDrawn.remove(next);
  render(draw.x()*(width/divisions), draw.y()*(height/divisions));
  if(toBeDrawn.isEmpty()) {
    rendering = false;
    save(saveFile);
  }
}

public void render(int startX, int startY) {
  for(int x = startX; x < startX+(width/divisions); x++) {
   for(int y = startY; y < startY+(height/divisions); y++) {
     setPixelColor(x, y);
     set(x, y, pixelColor);
   } 
  }
}

public void loadDraw() {
  for(int i = 0; i < divisions; i++) {
    for(int j = 0; j < divisions; j++) {
      toBeDrawn.add(new Pair(i, j));
    }
  }
}
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
    t = -1.0f;
    location = null;
    normal = null;
    surface = null;
  }
  
  public CollisionInfo clone() {
    return(new CollisionInfo(t, location.clone(), normal.clone(), surface.clone()));
  }
}
class Color {
  float[] colors;
  
  Color (float red, float green, float blue) {
    colors = new float[3];
    colors[0] = red;
    colors[1] = green;
    colors[2] = blue;
    regular();
  }
  
  public float getRed() {
    return colors[0];
  }
  
  public float getGreen() {
    return colors[1];
  }

  public float getBlue() {
    return colors[2];
  }
  
  public int getColor() {
    return color(colors[0], colors[1], colors[2]);
  }
  
  public void addColor(Color other) {
    for(int i = 0; i < colors.length; i++) {
      colors[i] += other.colors[i];
    }
    regular();
  }
  
  public Color scaleColor(float scale) {
    Color newColor = new Color(colors[0]*scale, colors[1]*scale, colors[2]*scale);
    return newColor;
  }
  
  public void regular() {
    for(Float value : colors) {
      value = min(value, 255.0f);
      value = max(value, 0.0f);
    }
  }
  
  public String toString() {
    return("[" + getRed() + "," + getGreen() + "," + getBlue() + "]");
  }
}
class Light {
  public Point location;
  public Color rgb;
  
  Light (float x, float y, float z, float r, float g, float b) {
    location = new Point(x, y, z);
    rgb = new Color(r, g, b);
  }
  
  //Does the ray hit this light?
  public boolean collision (Ray ray, ArrayList<Shape> shapes) {
    float closest = ray.nearestCollision(shapes).t;
    float distance = location.distance(ray.origin);
    if(closest < 0.0f || closest > distance) {return true;}
    else {return false;}
  }
  
  public float x() {
    return location.x(); 
  }
  
  public float y() {
    return location.y(); 
  }
  
  public float z() {
    return location.z(); 
  }
  
  public Color getColor() {
    return rgb;
  }
}
class Normal {
  public float[] normal;
  
  Normal (float x, float y, float z) {
    normal = new float[3];
    normal[0] = x;
    normal[1] = y;
    normal[2] = z;
    unit();
  }
  
  Normal(Point point) {
    normal = new float[3];
    normal[0] = point.point[0];
    normal[1] = point.point[1];
    normal[2] = point.point[2];
    unit();
  }
  
  public float x() {
    return normal[0];
  }
  
  public float y() {
    return normal[1];
  }
  
  public float z() {
    return normal[2];
  }
  
  public void unit() {
    float magnitude = (float) Math.sqrt((double)(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]));
    normal[0] = normal[0]/magnitude;
    normal[1] = normal[1]/magnitude;
    normal[2] = normal[2]/magnitude;
  }
  
  public Point scaleNormal(float scale) {
    Point newDirection = new Point(scale*normal[0], scale*normal[1], scale*normal[2]);
    return newDirection;
  }
  
  public Point addPoint(Point point) {
    Point newPoint = new Point(normal[0]+point.x(), normal[1]+point.y(), normal[2]+point.z());
    return point;
  }
  
  public Point subtract(Normal other) {
    return(new Point(x()-other.x(), y()-other.y(), z()-other.z()));
  }
  
  public Point subtract(Point point) {
    return(new Point(x()-point.x(), y()-point.y(), z()-point.z()));
  }
  
  public float dotProduct(Normal otherNormal) {
    return(normal[0]*otherNormal.x() + normal[1]*otherNormal.y() + normal[2]*otherNormal.z());
  }
  
  public Ray reflect(Ray incoming, Point location) {
    Ray outgoing;
    Normal direction = reflect(incoming);
    outgoing = new Ray(location, direction);
    return outgoing;
  }
  
  public Normal reflect(Ray incoming) {
    Normal outgoing = new Normal(incoming.direction.subtract(scaleNormal(2*dotProduct(incoming.direction))));
    return outgoing;
  }
  
  public Point toPoint() {
    return(new Point(x(), y(), z()));
  }
  
  public Normal clone() {
    return(new Normal(x(), y(), z()));
  }
  
  public Normal flip() {
    return(new Normal(-x(), -y(), -z()));
  }
  
  public String toString() {
    return("[" + x() + "," + y() + "," + z() + "]");
  }
}
class Pair {
  int[] point;
  
  Pair (int x, int y) {
    point = new int[2];
    point[0] = x;
    point[1] = y;
  }
  
  public int x() {
    return point[0];
  }
  
  public int y() {
    return point[1];
  }
}
class Point {
  public float[] point;
  
  Point (float x, float y, float z) {
    point = new float[3];
    point[0] = x;
    point[1] = y;
    point[2] = z;
  }
  
  public float x() {
    return point[0];
  }
  
  public float y() {
    return point[1];
  }
  
  public float z() {
    return point[2];
  }
  
  public Point addPoints(Point other) {
    Point newPoint = new Point(point[0]+other.point[0],
      point[1]+other.point[1], point[2]+other.point[2]);
    return newPoint;
  }
  
  //return this - other
  public Point subtractPoints(Point other) {
    Point newPoint = new Point(point[0] - other.point[0], 
      point[1] - other.point[1], point[2] - other.point[2]);
    return newPoint;
  }
  
  public Point scalePoint(float scale) {
    Point newPoint = new Point(scale*point[0], scale*point[1], scale*point[2]);
    return newPoint;
  }
  
  public float distance(Point other) {
    return(sqrt(pow(x() - other.x(), 2) + pow(y() - other.y(), 2) + pow(z() - other.z(), 2)));
  }
  
  public float dotProduct(Point other) {
    return(x()*other.x() + y()*other.y() + z()*other.z());
  }
  
  public Normal unit() {
    return(new Normal(x(), y(), z()));
  }
  
  public Point clone() {
    return(new Point(x(), y(), z()));
  }
  
  public String toString() {
    return("[" + x() + "," + y() + "," + z() + "]");
  }
}
class Ray {
  public Point origin;
  public Normal direction;
  
  Ray (Point origin, Normal direction) {
    this.origin = origin;
    this.direction = direction;
  }
  
  Ray (Point origin, Point direction) {
    this.origin = origin;
    this.direction = new Normal(direction);
  }
  
  Ray (float x0, float y0, float z0, float x1, float y1, float z1) {
    this(new Point(x0, y0, z0), new Normal(x1, y1, z1));
  }
  
  public float dotProduct(Ray ray) {
    return(direction.normal[0]*ray.direction.normal[0]
      + direction.normal[1]*ray.direction.normal[1] 
      + direction.normal[2]*ray.direction.normal[2]);
  }
  
  public Point scaleDirection(float scale) {
    return(direction.scaleNormal(scale));
  }
  
  //returns t = -1.0 if no collision occurs
  public CollisionInfo nearestCollision(ArrayList<Shape> shapes) {
    CollisionInfo info;
    CollisionInfo nearestCollision = new CollisionInfo();
    for(Shape shape : shapes) {
      info = shape.collision(this);
      if((nearestCollision.t < 0.0f && info.t > 0.0f) || (info.t < nearestCollision.t && info.t > 0.0f)) { //found a new closest collision point
        nearestCollision = info.clone();
      }
    }
    return nearestCollision;
  }
  
  public Normal unit() {
    float magnitude = sqrt(pow(direction.normal[0], 2.0f) 
      + pow(direction.normal[1], 2.0f) + pow(direction.normal[2], 2.0f));
    Normal normal = new Normal(direction.normal[0]/magnitude, 
      direction.normal[1]/magnitude, direction.normal[2]/magnitude);
    return normal;
  }
  
  public Ray flip() {
    Ray newRay = new Ray(origin, direction.scaleNormal(-1.0f));
    return newRay;
  }
  
  public String toString() {
    return("Origin: " + "[" + origin.x() + "," + origin.y() + "," + origin.z() + "]"
      + "\t\tDirection: " + "[" + direction.x() + "," + direction.y() + "," + direction.z() + "]");
  }
}
class Shape {
  Surface surface;
  
  Shape (Surface surface) {
    this.surface = surface;
  }
  
  public CollisionInfo collision(Ray ray) {
    return new CollisionInfo();
  }
}
class Sphere extends Shape {
  float radius;
  Point center;
  
  Sphere (Surface surface, float radius, Point center) {
    super(surface);
    this.radius = radius;
    this.center = center;
  }
  
  Sphere (Surface surface, float radius, float x, float y, float z) {
    this(surface, radius, new Point(x, y, z));
  }
  
  public @Override
  CollisionInfo collision(Ray ray) {
    float t;
    Point location;
    Normal normal;
    float a = ray.dotProduct(ray);
    Point origin = new Point(ray.origin.point[0], ray.origin.point[1], ray.origin.point[2]);
    Point CO = ray.origin.subtractPoints(center); //vector from center of sphere to origin of ray
    float b = CO.dotProduct(ray.scaleDirection(2.0f));
    float c = CO.dotProduct(CO) - radius*radius;
    float b24ac = b*b - 4.0f*a*c;
    if(b24ac < 0) {return (new CollisionInfo());}
    else if(b24ac == 0) {t = calcT(true, a, b, c, b24ac);}
    else {t = minPos(calcT(true, a, b, c, b24ac), calcT(false, a, b, c, b24ac));}
    location = origin.addPoints(ray.scaleDirection(t));
    normal = new Normal(location.subtractPoints(center));
    CollisionInfo collision = new CollisionInfo(t, location, normal, surface);
    return collision;
  }
  
  //returns the smallest positive float or -1.0 if both are negative
  public float minPos(float one, float two) {
    if(one < 0.0f && two < 0.0f) {return -1.0f;}
    if(one < 0.0f) {return two;}
    if(two < 0.0f) {return one;}
    else {return min(one, two);}
  }
  
  //quadratic formula (true for addition, false for subtraction)
  public float calcT(boolean plus, float a, float b, float c, float b24ac) {
    float t;
    if(plus) {
      t = (-b + sqrt(b24ac))/(2.0f*a);
    }
    else {
      t = (-b - sqrt(b24ac))/(2.0f*a);
    }
    return t;
  }
  
  public float radius() {
    return radius;
  }
  
  public float x() {
     return center.x(); 
  }
  
  public float y() {
     return center.y(); 
  }
  
  public float z() {
     return center.z(); 
  }
  
  public String toString() {
    return("Sphere with radius " + radius + " at " + center.toString());
  }
}
class Surface {
  public float[] diffuse;
  public float[] ambient;
  public float[] specular;
  public float P;
  public float Krefl;
  
  Surface (float Cdr, float Cdg, float Cdb, float Car, float Cag, float Cab, float Csr, float Csg, float Csb, float P, float Krefl) {
    diffuse = new float[3];
    ambient = new float[3];
    specular = new float[3];
    diffuse[0] = Cdr;
    diffuse[1] = Cdg;
    diffuse[2] = Cdb;
    ambient[0] = Car;
    ambient[1] = Cag;
    ambient[2] = Cab;
    specular[0] = Csr;
    specular[1] = Csg;
    specular[2] = Csb;
    this.P = P;
    this.Krefl = Krefl;
  }
  
  public Surface clone() {
    return(new Surface(Cdr(), Cdg(), Cdb(), Car(), Cag(), Cab(), Csr(), Csg(), Csb(), P(), Krefl()));
  }

  public float Cdr() {
    return diffuse[0];
  }
  
  public float Cdg() {
    return diffuse[1];
  }
  
  public float Cdb() {
    return diffuse[2];
  }
  
  public float Car() {
    return ambient[0];
  }
  
  public float Cag() {
    return ambient[1];
  }
  
  public float Cab() {
    return ambient[2];
  }
  
  public float Csr() {
    return specular[0];
  }
  
  public float Csg() {
    return specular[1];
  }
  
  public float Csb() {
    return specular[2];
  }
  
  public float P() {
    return P;
  }
  
  public float Krefl() {
    return Krefl;
  }
}
class Triangle extends Shape {
  ArrayList<Point> vertices;
  Point Q;
  Normal N;
  
  Triangle(Surface surface, ArrayList<Point> triangle) {
    super(surface);
    vertices = new ArrayList<Point>();
    for(int i = 0; i < 3; i++) {
      vertices.add(triangle.get(i).clone());
    }
    setQN();
  }
  
  public CollisionInfo collision(Ray ray) {
    CollisionInfo planeInfo = plane(ray);
    if(planeInfo.t < 0.0f) {return (new CollisionInfo());}
    if(halfPlane(planeInfo)) {return planeInfo;}
    return (new CollisionInfo());
  }
  
  //returns true if the point where the ray collides with the plane is inside of the triangle
  public boolean halfPlane(CollisionInfo planeInfo) {
    Point P = planeInfo.location.clone();
    Point A = vertices.get(0).clone();
    Point B = vertices.get(1).clone();
    Point C = vertices.get(2).clone();
    boolean sameSidePA = sameSide(P, A, C, B);
    boolean sameSidePB = sameSide(P, B, C, A);
    boolean sameSidePC = sameSide(P, C, A, B);
    return (sameSidePA && sameSidePB && sameSidePC);
  }
  
  //Returns true if P and A are on the same side of line BC
  public boolean sameSide(Point P, Point A, Point B, Point C) {
    Point lineBC = C.subtractPoints(B);
    Point lineBA = A.subtractPoints(B);
    Point lineBP = P.subtractPoints(B);
    Point productA = crossProduct(lineBC, lineBA);
    Point productP = crossProduct(lineBC, lineBP);
    float direction = productA.dotProduct(productP);
    if(direction >= 0.0f) {return true;}
    else {return false;}
  }
  
  //Returns CollisionInfo for the plane (chooses between 2 possible normals)
  public CollisionInfo plane(Ray ray) {
    Point location;
    Point QO = ray.origin.subtractPoints(Q);
    Point normal = N.toPoint();
    float denominator = (ray.direction.toPoint().dotProduct(normal));
    if(denominator == 0.0f) {return (new CollisionInfo());}
    float t = ((-(QO.dotProduct(normal))) / denominator);
    if(t < 0.0f) {return (new CollisionInfo());}
    location = ray.origin.addPoints(ray.scaleDirection(t));
    Normal newNormal = chooseNormal(N, ray);
    return (new CollisionInfo(t, location, newNormal, surface));
  }
  
  public Normal chooseNormal(Normal normal, Ray ray) {
    float up = ray.direction.dotProduct(N);
    float down = ray.direction.dotProduct(N.flip());
    if(up <= down) {return normal.clone();}
    else {return normal.flip();}
  }
  
  public void setQN() {
    Q = vertices.get(0).clone();
    crossProduct();
  }
  
  // (C - B) X (A - B)
  public Point crossProduct(Point BC, Point BA) {
    float i = BC.y() * BA.z() - BC.z() * BA.y();
    float j = BC.x() * BA.z() - BC.z() * BA.x();
    float k = BC.x() * BA.y() - BC.y() * BA.x();
    Point product = new Point(i, j, k);
    return product;
  }
  
  // (vertex3 - vertex1) X (vertex2 - vertex1)
  public void crossProduct() {
    Point line13 = vertices.get(2).subtractPoints(Q);
    Point line12 = vertices.get(1).subtractPoints(Q);
    float i = (line13.y() * line12.z()) - (line13.z() * line12.y());
    float j = -((line13.x() * line12.z()) - (line13.z() * line12.x()));
    float k = (line13.x() * line12.y()) - (line13.y() * line12.x());
    N = new Normal(i, j, k);
  }
  
  public void add(Point p) {
    if(vertices.size() < 3) {
      vertices.add(p);
    }
    else {
      println("Too many vertices for a triangle");
    }
  }
  
  public void add(float x, float y, float z) {
    Point p = new Point(x, y, z);
    add(p);
  }
  
  public Point vertexA() {
    return vertices.get(0);
  }
  
  public Point vertexB() {
    return vertices.get(1);
  }
  
  public Point vertexC() {
    return vertices.get(2);
  }
  
  public String toString() {
    String s = "";
    for(Point point : vertices) {
      s += (point.toString() + "\t");
    }
    return s;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ray_parser" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
