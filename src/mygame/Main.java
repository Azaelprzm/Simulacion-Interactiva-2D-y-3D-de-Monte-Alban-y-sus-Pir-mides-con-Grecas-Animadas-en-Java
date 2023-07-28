package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication
        implements ActionListener {

    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        DirectionalLight sun = new DirectionalLight();

        AmbientLight sun2 = new AmbientLight();
        sun2.setEnabled(true);

        sun.setColor(ColorRGBA.White.mult(0.2f));
        sun.setEnabled(true);
        rootNode.addLight(sun);
        flyCam.setMoveSpeed(60);
        
        // Agregar el cielo (Skybox)
        Texture west = assetManager.loadTexture("Textures/Skybox/txt_igual.png");
        Texture east = assetManager.loadTexture("Textures/Skybox/txt_igual.png");
        Texture north = assetManager.loadTexture("Textures/Skybox/txt_igual.png");
        Texture south = assetManager.loadTexture("Textures/Skybox/txt_igual.png");
        Texture up = assetManager.loadTexture("Textures/Skybox/txt_igual.png");
        Texture down = assetManager.loadTexture("Textures/Skybox/txt_igual.png");
        
        Spatial sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down);
        rootNode.attachChild(sky);

        Spatial casona;
        casona = assetManager.loadModel("Models/MonteAlban_SinTecho2.j3o");
        casona.setLocalScale(2f);
        rootNode.attachChild(casona);

        DirectionalLight sun3 = new DirectionalLight();
        sun3.setDirection(new Vector3f(-5, -40, -4).normalizeLocal());
        sun3.setColor(ColorRGBA.White);
        rootNode.addLight(sun3);

        // Añade una luz ambiental para iluminar todos los objetos de la escena
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);

        /**
          * Configurar Física
          */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0f, 0f, 0f, 0f));
        flyCam.setMoveSpeed(20);
        setUpKeys();
        setUpLight();
        
        // Configuramos la detección de colisiones para la escena creando un
         // forma de colisión compuesta y un RigidBodyControl estático con masa cero.
        CollisionShape sceneShape
                = CollisionShapeFactory.createMeshShape((Node) casona);
        landscape = new RigidBodyControl(sceneShape, 0);
        casona.addControl(landscape);
        
        // Configuramos la detección de colisiones para el jugador creando
         // una forma de colisión de cápsula y un CharacterControl.
         // El CharacterControl ofrece ajustes adicionales para
         // tamaño, altura de paso, salto, caída y gravedad.
         // También ponemos al jugador en su posición inicial.
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(0.5f, 2f, 1);
        player = new CharacterControl(capsuleShape, 0.4f);
        player.setJumpSpeed(5);
        player.setFallSpeed(10);
        player.setGravity(9);
        player.setPhysicsLocation(new Vector3f(0, 5, 0));
        
        // Adjuntamos la escena y el jugador al nodo raíz y al espacio de física,
         // para que aparezcan en el mundo del juego.
        rootNode.attachChild(casona);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);
    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }

    /**
      * Sobreescribimos algunas asignaciones de teclas de navegación aquí, para que podamos agregar
      * caminar y saltar controlados por la física:
      */
    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
    }

    /**
     * These are our custom actions triggered by key presses. We do not walk
     * yet, we just keep track of the direction the user pressed.
     */
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            if (value) {
                left = true;
            } else {
                left = false;
            }
        } else if (binding.equals("Right")) {
            if (value) {
                right = true;
            } else {
                right = false;
            }
        } else if (binding.equals("Up")) {
            if (value) {
                up = true;
            } else {
                up = false;
            }
        } else if (binding.equals("Down")) {
            if (value) {
                down = true;
            } else {
                down = false;
            }
        } else if (binding.equals("Jump")) {
            player.jump();
        }
    }

    /**
      * Este es el ciclo principal del evento: caminar ocurre aquí. Comprobamos en qué
      * dirección en la que camina el jugador interpretando la dirección de la cámara
      * adelante (camDir) y al costado (camLeft). El setWalkDirection()
      * El comando es lo que permite caminar a un jugador controlado por la física. También nos aseguramos
      * aquí que la cámara se mueve con el jugador.
      */
    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.2f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }
}

