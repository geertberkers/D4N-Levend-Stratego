using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class newRole : MonoBehaviour {

    public Sprite Mouse;
    public Sprite Bunny;
    public Sprite Pig;
    public Sprite Fox;
    public Sprite Frog;

    public Image You;
    public Image Prey1;
    public Image Prey2;
    public Image Hunter1;
    public Image Hunter2;

    // Use this for initialization
    void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
	
	}


    public void assignNewRole()
    {
        int rand = UnityEngine.Random.Range(0, 5);
        if (rand == 0)
        {
            You.sprite = Mouse;
            Prey1.sprite = Frog;
            Prey2.sprite = Fox;
            Hunter1.sprite = Pig;
            Hunter2.sprite = Bunny;
        }
        else if (rand == 1)
        {
            You.sprite = Frog;
            Prey1.sprite = Fox;
            Prey2.sprite = Pig;
            Hunter1.sprite = Bunny;
            Hunter2.sprite = Mouse;
        }
        else if (rand == 2)
        {
            You.sprite = Fox;
            Prey1.sprite = Pig;
            Prey2.sprite = Bunny;
            Hunter1.sprite = Frog;
            Hunter2.sprite = Mouse;
        }
        else if (rand == 3)
        {
            You.sprite = Pig;
            Prey1.sprite = Bunny;
            Prey2.sprite = Mouse;
            Hunter1.sprite = Fox;
            Hunter2.sprite = Frog;
        }
        else if (rand == 4)
        {
            You.sprite = Bunny;
            Prey1.sprite = Mouse;
            Prey2.sprite = Frog;
            Hunter1.sprite = Pig;
            Hunter2.sprite = Fox;
        }
    }
}
