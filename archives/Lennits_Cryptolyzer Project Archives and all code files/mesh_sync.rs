use libp2p::{mdns, kad, Swarm, swarm::SwarmEvent};
use std::error::Error;

pub async fn bootstrap_swarm() -> Result<(), Box<dyn Error>> {
    // 1. Create a PeerID based on your Quantum-Safe Public Key
    let local_key = libp2p::identity::Keypair::generate_ed25519();
    let local_peer_id = libp2p::PeerId::from(local_key.public());
    println!("> INITIALIZING_SWARM_NODE: {}", local_peer_id);

    // 2. Setup Transport (TCP + Noise Encryption + Yamux Muxing)
    let mut swarm = libp2p::SwarmBuilder::with_new_identity(local_key)
        .with_tokio()
        .with_tcp(Default::default(), (libp2p::noise::Config::new, libp2p::yamux::Config::default))?
        .with_behaviour(|key| {
            let kademlia = kad::Behaviour::new(key.public().to_peer_id(), kad::store::MemoryStore::new(key.public().to_peer_id()));
            let mdns = mdns::tokio::Behaviour::new(mdns::Config::default(), key.public().to_peer_id())?;
            Ok(MyBehaviour { kademlia, mdns })
        })?
        .build();

    // 3. Connect to the Genesis Society Seed (Your Domain)
    let seed_addr = "/dns4/lstechnologies.cu.ma/tcp/4001".parse()?;
    swarm.dial(seed_addr)?;

    // 4. Listen for Peer Events
    loop {
        match swarm.select_next_some().await {
            SwarmEvent::Behaviour(MyBehaviourEvent::Mdns(mdns::Event::Discovered(list))) => {
                for (peer_id, multiaddr) in list {
                    println!("> DISCOVERED_PEER: {} at {}", peer_id, multiaddr);
                    swarm.behaviour_mut().kademlia.add_address(&peer_id, multiaddr);
                }
            }
            SwarmEvent::NewListenAddr { address, .. } => {
                println!("> NODE_LISTENING: {}", address);
            }
            _ => {}
        }
    }
}
