///////////////////////////////////////////////////////////////////////
//
// Command Line Interface (CLI) Parser  
//
///////////////////////////////////////////////////////////////////////
import java.util.*;

String gCurrentFile = new String("rect_test.cli"); // A global variable for holding current active file name.
final float aspect = 1.0;
float fov;
float centerX;
float centerY;
Color background;
color pixelColor;
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
void keyPressed() {
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
void interpreter() {
  loadDefaults();
  
  String str[] = loadStrings(gCurrentFile);
  if (str == null) println("Error! Failed to read the file.");
  for (int i=0; i<str.length; i++) {
    
    String[] token = splitTokens(str[i], " "); // Get a line and parse tokens.
    if (token.length == 0) continue; // Skip blank line.
    
    if (token[0].equals("fov")) {
      fov = parseValue(token[1]) * (PI / 180.0);
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
      float r =float(token[1]);
      float g =float(token[2]);
      float b =float(token[3]);
      fill(r, g, b);
    }
    else if (token[0].equals("rect")) {
      float x0 = float(token[1])*(height/300);
      float y0 = float(token[2])*(height/300);
      float x1 = float(token[3])*(height/300);
      float y1 = float(token[4])*(height/300);
      rect(x0, height-y1, x1-x0, y1-y0);
    }
    else if(gCurrentFile.equals("rect_test.cli")) {save(gCurrentFile);}
    
    //This is where the rendering occurs
    else if (token[0].equals("write") && !gCurrentFile.equals("rect_test.cli")) {
      saveFile = token[1];
      rendering = true;
    }
  }
}

Float parseValue(String s) {
  Float value;
  try {
    value = Float.parseFloat(s);
  }
  catch(Exception e) {
    value = (float) Integer.parseInt(s);
  }
  return value;
}

void setPixelColor(int i, int j) {
  origin = new Point(viewer.x(), viewer.y(), viewer.z());
  direction = generateNormal(i, j);
  Ray ray = new Ray(origin, direction);
  Color shade = traceRay(ray, 0); //determine the color viewed at this pixel
  pixelColor = color(shade.getRed(), shade.getGreen(), shade.getBlue());
}

//returns the color seen along the specified ray
Color traceRay(Ray ray, int iteration) {
  if(counter > timeout) {return background;}
  counter++;
  Ray reflectedRay;
  CollisionInfo info = ray.nearestCollision(shapes);
  Color shade; //the color of outgoing light (along path toward viewer away from collision)
  Color reflected; //the color of incoming light (along path toward viewer toward collision)
  Color spectral = new Color(0.0, 0.0, 0.0); //specular highlight component
  Color diffuse = new Color(0.0, 0.0, 0.0); //normalized sum of all incoming light (along any path that hits the collision point)
  Point originSlide;

  //find reflected color
  if(info.t < 0.0 || iteration > 50) { //reflection hits nothing or too many reflections have occurred
    return background;
  }
  else { //reflection hits another object so we find the incoming light on the reflection path
      originSlide = info.normal.scaleNormal(0.0001).addPoints(info.location); //start slightly off of the surface so that new ray does not intersect the current surface
      reflectedRay = info.normal.reflect(ray, originSlide); //reflects the traced ray across the collision normal
      reflected = traceRay(reflectedRay, iteration+1); //recurse to find the incoming color for reflection
  }

  //find spectral and diffuse
  for(Light light : lights) {
    Normal V = ray.flip().direction; //ray from surface to eye
    Ray L = new Ray(originSlide, light.location.subtractPoints(originSlide)); //ray from surface to light source
    Normal R = info.normal.reflect(L.flip()); //reflection of ray from light source off of object roghly toward eye
    
    if(light.collision(L, shapes)) {
      float Kspec = pow(max(0.0, V.dotProduct(R)), info.surface.P); //(V*R)^P

      spectral.addColor(light.rgb.scaleColor(Kspec)); //multiply color by Kspec
    
      //find diffuse component
      diffuse.addColor(light.rgb.scaleColor(max(0.0, info.normal.dotProduct(L.unit())))); //Cl*max(0, N*L)
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

Normal generateNormal(int i, int j) {
  float t = tan(fov/2);
  float z = 1;
  float xPan = t * ((i - centerX) / (height/2));
  float yPan = t * ((centerY - j) / (height/2));
  float zPan = -1;
  return (new Normal(xPan, yPan, zPan));
}

float pythagorean(float x, float y) {
  return (sqrt(sq(x) + sq(y)));
}

float pythagorean(float x, float y, float z) {
  return (sqrt(sq(x) + sq(y) + sq(z)));
}

void loadDefaults() {
  rendering = false;
  counter = 0;
  triangleCollision = 0;
  sphereCollision = 0;
  fov = 60.0;
  background = new Color(0.0, 0.0, 0.0);
  lights.clear();
  shapes.clear();
  surface = new Surface(0.8, 0.8, 0.8, 0.2, 0.2, 0.2, 0.0, 0.0, 0.0, 2.0, 0.5);
  viewer = new Point(0.0, 0.0, 0.0);
  origin = new Point(0.0, 0.0, 0.0);
  direction = new Normal(0.0, 0.0, -1.0);
  pixelColor = color(0, 0, 0);
  xDivision = 0;
  yDivision = 0;
  slideDirection = 1;
  toBeDrawn.clear();
  loadDraw();
  renderType = rand.nextInt(renderTypes);
}

void printSettings() {
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
void setup() {
  size(1000, 800);
  centerX = (width - 1) / 2;
  centerY = (height - 1) / 2;
  noStroke();
  colorMode(RGB, 1.0);
  background(0, 0, 0);
  interpreter();
}

///////////////////////////////////////////////////////////////////////
//
// Draw frames.  Should leave this empty.
//
///////////////////////////////////////////////////////////////////////
void draw() {
  if(gCurrentFile.equals("rect_test.cli")) {
    String s = "Each number key displays a unique rendering";
    color green = color(0, 255, 0);
    fill(green);
    textSize(30);
    text(s, 10, 10, width, 36);
  }
  if(rendering == true) {
    chooseRender();
  }
}

void chooseRender() {
  switch(renderType) {
    case 0: slideRender(); break;
    case 1: randomRender(); break;
  }
}

void slideRender() {
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

void randomRender() {
  int next = rand.nextInt(toBeDrawn.size());
  Pair draw = toBeDrawn.remove(next);
  render(draw.x()*(width/divisions), draw.y()*(height/divisions));
  if(toBeDrawn.isEmpty()) {
    rendering = false;
    save(saveFile);
  }
}

void render(int startX, int startY) {
  for(int x = startX; x < startX+(width/divisions); x++) {
   for(int y = startY; y < startY+(height/divisions); y++) {
     setPixelColor(x, y);
     set(x, y, pixelColor);
   } 
  }
}

void loadDraw() {
  for(int i = 0; i < divisions; i++) {
    for(int j = 0; j < divisions; j++) {
      toBeDrawn.add(new Pair(i, j));
    }
  }
}
