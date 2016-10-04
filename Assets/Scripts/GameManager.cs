﻿using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using UnityEngine.SceneManagement;

public class GameManager : MonoBehaviour {

    //Singleton
    private static GameManager instance = null;
    public static GameManager Instance { get { return instance; } }

    public Image You;
    public Image Prey1;
    public Image Prey2;
    public Image Hunter1;
    public Image Hunter2;

    public Sprite RabbitImage;
    public Sprite MouseImage;
    public Sprite FrogImage;
    public Sprite FoxImage;
    public Sprite PigImage;

    private Animal[] animals;

    Animal Rabbit = new Animal();
    Animal Mouse = new Animal();
    Animal Frog = new Animal();
    Animal Fox = new Animal();
    Animal Pig = new Animal();

    void Awake()
    {
        if (instance != null && instance != this)
            Destroy(this.gameObject);
        instance = this;
        DontDestroyOnLoad(this.gameObject);
    }

    void Start()
    {
        Rabbit.Prey1 = Mouse; Rabbit.Prey2 = Frog; Rabbit.Hunter1 = Fox; Rabbit.Hunter2 = Pig; Rabbit.Image = RabbitImage;
        Mouse.Prey1 = Frog; Mouse.Prey2 = Fox; Mouse.Hunter1 = Rabbit; Mouse.Hunter2 = Pig; Mouse.Image = MouseImage;
        Frog.Prey1 = Fox; Frog.Prey2 = Pig; Frog.Hunter1 = Mouse; Frog.Hunter2 = Rabbit; Frog.Image = FrogImage;
        Fox.Prey1 = Pig; Fox.Prey2 = Rabbit; Fox.Hunter1 = Frog; Fox.Hunter2 = Mouse; Fox.Image = FoxImage;
        Pig.Prey1 = Rabbit; Pig.Prey2 = Mouse; Pig.Hunter1 = Fox; Pig.Hunter2 = Frog; Pig.Image = PigImage; 

        animals = new Animal[5];
        animals[0] = Rabbit;
        animals[1] = Mouse;
        animals[2] = Frog;
        animals[3] = Fox;
        animals[4] = Pig;
    }

    public void ScanQRCode()
    {
        SceneManager.LoadScene("QRScanner", LoadSceneMode.Additive);
    }

    public void AssignNewRole()
    {
        if (animals.Length == 0)
            Debug.LogError("Animal list is empty");

        int random = UnityEngine.Random.Range(0, 5);
        You.sprite = animals[random].Image;
        Prey1.sprite = animals[random].Prey1.Image;
        Prey2.sprite = animals[random].Prey2.Image;
        Hunter1.sprite = animals[random].Hunter1.Image;
        Hunter2.sprite = animals[random].Hunter2.Image;
    }
}